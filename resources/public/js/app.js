var app = (function(L,B,_) {
    var app = {};

    // Classes
    app.models = {};
    app.views = {};

    // Runtime objects
    app.rt = {};
    app.rt.views = {};
    app.rt.models = {};

    app.models.FrameRef = B.Model.extend({
        frame: 0, 
        distance: 0, 
        pt: {lat: 0, lng:0}
    });

    // The global frame ref represents the location
    // of a given frame at any given time
    app.rt.models.frame = new app.models.FrameRef({'frame': 0});

    // The selected frame ref represents a given frame, distance
    // and position tuple once selected. The general workflow is
    // to promote this to the saved list once the user says it is
    // golden
    app.rt.models.selectedFrame = new app.models.FrameRef({});


    app.models.FrameRefs = B.Collection.extend({
        model: app.models.FrameRef,
        comparator: function(ref) {
            return ref.get('frame');
        },
        url: '/references'
    });

    app.models.Shapes = B.Collection.extend({
        model: function(m) {
            return new B.Model({'lat': m[0], 'lng': m[1]});
        },
        url: '/shape'
    });

    app.views.FrameViewer = Backbone.View.extend({
        tagName: 'div',
        className: 'user-defined-frames',

        initialize: function() {
            this.listenTo(this.model, "change", this.render);
        },

        render: function() {
            this.$el.html('Frame: ' + this.model.get('frame'));
        }
    });

    app.views.RouteLineView = Backbone.View.extend({
        initialize: function() {
            this.listenTo(this.collection, "all", this.render);
            this.map = this.options.map;
        },

        render: function() {
            if (this.line) {
                this.map.removeLayer(this.line);
            }

            this.line = L.polyline(this.collection.map(function(t) {
                return new L.LatLng(t.get('lat'), t.get('lng'));
            }));

            this.map.addLayer(this.line);
        }
    });

    app.views.RoutePointsView = Backbone.View.extend({
        initialize: function() {
            this.listenTo(this.collection, "all", this.render);
            this.map = this.options.map;
        },

        render: function() {
            var that = this;
            if (this.markers) {
                _.map(this.markers, function(m) {
                    that.map.removeLayer(m)
                });
            }
            
            this.markers = [];
            this.collection.map(function(t) {
                var pt = t.get('pt');
                var marker = new L.Marker(new L.LatLng(pt.lat,pt.lng))
                that.markers.push(marker);
                that.map.addLayer(marker);
            });
        }
    });

    app.views.UserDefinedFramesView = Backbone.View.extend({
        tagName: 'ul',

        className: 'user-defined-frames',

        events: {
            'click .delete': 'deleteRec',
            'click .edit': 'editRec',
            'click .new': 'newRec'
        },

        initialize: function() {
            this.listenTo(this.collection, 'all', this.render);
            this.template = _.template("<li><%= frame %> @ <%= distance %></li>");
        },

        deleteRec: function() {

        },

        editRec: function() {

        },

        newRec: function() {

        },

        render: function() {
            this.$el.empty();

            var that = this;
            this.$el = this.collection.reduce(function($el, model) {
                return $el.append(that.template(model.attributes));
            }, this.$el);
        }
    });                                 

    app.createMapClickHandler = function(frameModel, selectModel) {
        return function(e) {
            var frame = frameModel.get('frame');
            $.ajax('/snap-to-route',
                   {
                       data: {
                           'lat': e.latlng.lat,
                           'lng': e.latlng.lng
                       }
                   }).done(function(ll) {
                       app.rt.map.addLayer(L.marker(new L.LatLng(ll.pt.lat,ll.pt.lng)));
                       ll.frame = frame;
                       selectModel.set(ll);
                   });
        };
    };
        

    function init() {        
        app.rt.map = L.map('map').setView([40.767, -74.01], 13);

        app.rt.map.on('click', 
                      app.createMapClickHandler(
                          app.rt.models.frame,
                          app.rt.models.selectedFrame));

        L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
            attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
        }).addTo(app.rt.map);

        app.rt.models.shapes = new app.models.Shapes();
        app.rt.views.shapeview = new app.views.RouteLineView({
            'map': app.rt.map,
            'collection': app.rt.models.shapes
        });

        app.rt.models.shapes.fetch();

        app.rt.models.userFrames = new app.models.FrameRefs();
        app.rt.views.userFrames = new app.views.UserDefinedFramesView({
            'collection': app.rt.models.userFrames
        });
        app.rt.views.userPoints = new app.views.RoutePointsView({
            'collection': app.rt.models.userFrames,
            'map': app.rt.map
        });

        $("#userlist").append(app.rt.views.userFrames.el);

        app.rt.models.userFrames.fetch();

        app.rt.views.frameView = new app.views.FrameViewer({'model': app.rt.models.frame});

        $("#curframe").append(app.rt.views.frameView.el);
    }

    app.init = init;

    app.ytInit = function() {
        app.yt = {};
        app.yt.onPlayerReady = function(event) {
            var player = event.target;
            var updateFn = function() {               
                app.rt.models.frame.set('frame', Math.floor(player.getCurrentTime() * 24.0));
                setTimeout(updateFn, 40);
            };

            updateFn();
        };
        app.yt.onPlayerStateChange = function() {};

        app.rt.player = new YT.Player('player', {
            height: '350',
            width: '350',
            videoId: 'cmaLggM73a0',
            events: {
                'onReady': app.yt.onPlayerReady,
                'onStateChange': app.yt.onPlayerStateChange
            }
        });
    }

    return app;
}(L,Backbone,_))

onYouTubeIframeAPIReady = app.ytInit;
$(app.init);
