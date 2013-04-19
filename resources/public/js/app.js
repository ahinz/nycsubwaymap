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

    function init() {        
        app.rt.map = L.map('map').setView([40.767, -74.01], 13);

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
    }

    app.init = init;

    return app;
}(L,Backbone,_))

$(app.init);
