var app = (function(L,B) {
    var app = {};

    app.models = {};
    app.views = {};
    app.rt = {}; // Runtime
    app.rt.views = {};
    app.rt.models = {};

    app.models.FrameRef = B.Model.extend({ frame: 0, distance: 0 });
    app.models.FrameRefs = B.Collection.extend({
        model: app.models.FrameRef,
        comparator: function(ref) {
            return ref.get('frame');
        }
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
    }

    app.init = init;

    return app;
}(L,Backbone))

$(app.init);
