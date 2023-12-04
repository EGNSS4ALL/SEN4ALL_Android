var map;
var firstLoc;

var ISANDROID = true;

var drawInteraction;
var drawingOverlay;
var mapLayer;
var locationLayer;
var contextLayer;
var contextSwitchLayer;
var drawLayer;
var wasContextLayer = true;
var justStarted = true;
var lastLayer;
var lastRangeDate;
var currentLayer;

window.addEventListener('load', function () {

    initDb();
    let _zoom = GetURLParameter("zoom");
    let _minZoom = GetURLParameter("minZoom");
    let _maxZoom = GetURLParameter("maxZoom");
    let _lat = GetURLParameter("lat");
    let _lon = GetURLParameter("lon");
    let center3857 = [1391602.982331, 5146180.257071];
    if(typeof _lat !== 'undefined' && _lat != null && _lat != ""){
        center3857 = ol.proj.fromLonLat([parseFloat(_lon), parseFloat(_lat)], ol.proj.get("EPSG:3857"));
    }

    mapLayer =  new ol.layer.Tile({ preload: Infinity, source: new ol.source.OSM({ tileLoadFunction: tileLoadCache, cacheSize: 0, crossOrigin: 'anonymous'})});

    locationLayer = new ol.layer.Vector({
                                zIndex : 5,
                                title: "location",
                                source: new ol.source.Vector({
                                        features: []
                                })
                            })

    contextLayer = new ol.layer.Tile({ preload: 0 });

    contextSwitchLayer = new ol.layer.Tile({ preload: 0 });

    drawLayer = new ol.layer.Vector({
          source:  new ol.source.Vector({ features: []}),
          style: new ol.style.Style({
             fill: new ol.style.Fill({
               color: 'rgba(255, 255, 255, 0.5)',
             }),
             stroke: new ol.style.Stroke({
               color: 'rgba(255, 136, 0, 1)',
               width: 2,
             }),
          }),
          name: 'drawPolygon'
     });

    map = new ol.Map({
      layers: [mapLayer, locationLayer, contextLayer, contextSwitchLayer, drawLayer ],
      controls: [],
      target: 'map',
      loadTilesWhileAnimating: true,
      loadTilesWhileInteracting: true,
      view: new ol.View({
        center: center3857,
        zoom: parseInt(_zoom),
        minZoom: parseInt(_minZoom),
        maxZoom: parseInt(_maxZoom)
      })
    });

    map.on("moveend", function(evt){
        let currentzoom = map.getView().getZoom();
        if(typeof currentzoom !== 'undefined' && currentzoom != null){
        	if(ISANDROID)
            	WebViewBridge.setZoom(map.getView().getZoom());
        }

        let coord = map.getView().getCenter();
        let coordinates = ol.proj.transform(coord, "EPSG:3857", "EPSG:4326");
        let C = 40075016.686;
        let stile =
          (C * Math.cos(degrees_to_radians(coordinates[1]))) /
          Math.pow(2, currentzoom);
        let spixel = stile / 256;
        if(typeof spixel !== 'undefined' && spixel != null){
        	if(ISANDROID){
        		WebViewBridge.setPixelSize(spixel);
        	}else{
        		var dati = {zoom: currentzoom, pixel: spixel};
            	var datiJSON = JSON.stringify(dati);
            	window.webkit.messageHandlers.zoomAndPixel.postMessage(datiJSON);
        	}
        }
    });

    drawingOverlay = new ol.Overlay({
        element: document.getElementById('info'),
        positioning: 'bottom-left'
    });
    drawingOverlay.setMap(map);
    drawingOverlay.setVisible(false);

})

function setCenterOld(latitude, longitude){
    let center3857 = ol.proj.transform([longitude, latitude], ol.proj.get("EPSG:4326"), ol.proj.get("EPSG:3857"));
    map.getView().animate({
            center: center3857,
            duration: 500,
            rotation: 0,
            zoom: map.getView().getZoom()
        });
}

var locationPinSrc = "data:image/png;base64,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";

function setCenter(lat,lon){

    let loc = ol.proj.fromLonLat([parseFloat(lon), parseFloat(lat)], map.getView().getProjection().getCode());
    if(typeof firstLoc === 'undefined'){
        createLocFeature();
    }

    if(typeof firstLoc.getGeometry() !== 'undefined'){
        firstLoc.getGeometry().setCoordinates(loc);
    }else{
        firstLoc.setGeometry(new ol.geom.Point(loc));
    }
    if(map && !map.getView().getAnimating()){
        map.getView().animate({
                    center: loc,
                    duration: 500,
                    rotation: 0,
                    zoom: map.getView().getZoom()
                });
    }
}

function createLocFeature(){
    firstLoc = new ol.Feature();
	firstLoc.setId("myPosition1");
	firstLoc.setStyle(function(feat, res){
		return new ol.style.Style({
      			image: new ol.style.Icon({
      				src: locationPinSrc, //"location.png", //icon,
                      anchor: [0.5, 1],
                      anchorXUnits: 'fraction',
                      anchorYUnits: 'fraction',
                      opacity: 1,
                      scale: 0.4,
                      rotateWithView: false,
                      crossOrigin: "anonymous"
      		      })
      		})
     })
     map.getLayers().getArray()[1].getSource().addFeature(firstLoc);
}

function setLayer(layerName, rangeDate){

    console.log("SETLAYER "+layerName);
    currentLayer = layerName;
        let legendUrl;
        let layerOpacity;
        let newSource;

       if(layerName == '3_NDVI'){
            legendUrl = "https://<your layer legend url>";
            newSource = new ol.source.TileWMS({
                cacheSize: 0,
                url: "https://<your layer service provider url>",
                params: {"maxcc":80,"minZoom":6,"maxZoom":16,"preset":"3_NDVI","layers":"3_NDVI","time": rangeDate},
                tileLoadFunction: layerTileLoad,
                transition: 0,
                crossOrigin: 'anonymous'
            });
            layerOpacity = 0.4;
       }else if(layerName == 'ENHANCED-VISUALIZATION'){
            legendUrl = "https://<your layer legend url>";
            newSource = new ol.source.TileWMS({
               cacheSize: 0,
               url: "https://<your layer service provider url>",
               params: { "maxcc":100,"minZoom":6,"maxZoom":16,"preset":"ENHANCED-VISUALIZATION","layers":"ENHANCED-VISUALIZATION", "time": rangeDate },
               tileLoadFunction: layerTileLoad,
               transition: 0,
               crossOrigin: 'anonymous'
            });
            layerOpacity = 0.6;
       }else if(layerName == 'URBAN-DETECTION'){
            legendUrl = "https://<your layer legend url>";
            newSource = new ol.source.TileWMS({
                cacheSize: 0,
                url: "https://<your layer service provider url>",
                params: { "maxcc": 100, "minZoom": 6, "maxZoom": 16, "preset": "URBAN-DETECTION", "layers": "URBAN-DETECTION", "time": rangeDate },
                tileLoadFunction: layerTileLoad,
                transition: 0,
                crossOrigin: 'anonymous'
              });
              layerOpacity = 0.6;
       }else if(layerName == 'MOISTURE'){
            legendUrl = "https://<your layer legend url>";
            newSource = new ol.source.TileWMS({
               cacheSize: 0,
               url: "https://<your layer service provider url>",
               params: {"maxcc":80,"minZoom":6,"maxZoom":16,"preset":"MOISTURE","layers":"MOISTURE","time": rangeDate},
               tileLoadFunction: layerTileLoad,
               transition: 0,
               crossOrigin: 'anonymous'
             });
             layerOpacity = 0.8;
       }else if(layerName == 'WATER-ROUGHNESS' ){
            legendUrl = "https://<your layer legend url>";
            newSource = new ol.source.TileWMS({
               cacheSize: 0,
               url: "https://<your layer service provider url>",
               params: { "maxcc": "100", "minZoom": 6, "maxZoom": 16, "preset": "WATER-ROUGHNESS", "layers": "WATER-ROUGHNESS", "time": rangeDate },
               tileLoadFunction: layerTileLoad,
               transition: 0,
               crossOrigin: 'anonymous'
             });
             layerOpacity = 0.8;
       }else if(layerName == 'IW_VV'){
            legendUrl = "https://<your layer legend url>";
            newSource = new ol.source.TileWMS({
               cacheSize: 0,
               url: "https://<your layer service provider url>",
               params: { "maxcc": "100", "minZoom": 6, "maxZoom": 16, "preset": "IW_VV", "layers": "IW_VV", "time": rangeDate },
               tileLoadFunction: layerTileLoad,
               transition: 0,
               crossOrigin: 'anonymous'
             });
             layerOpacity = 0.6;
       }else if(layerName == 'IW_VH'){
            legendUrl = "https://<your layer legend url>";
            newSource = new ol.source.TileWMS({
                cacheSize: 0,
                url: "https://<your layer service provider url>",
                params: { "maxcc": 100, "minZoom": 6, "maxZoom": 16, "preset": "IW_VH", "layers": "IW_VH", "time": rangeDate },
                tileLoadFunction: layerTileLoad,
                transition: 0,
                crossOrigin: 'anonymous'
            });
            layerOpacity = 0.6;
       }else if(layerName == 'SO2'){
            legendUrl = "https://<your layer legend url>";
            newSource = new ol.source.TileWMS({
                cacheSize: 0,
                url: "https://<your layer service provider url>",
                params: {"maxcc": 100, "minZoom": 6, "maxZoom": 16, "preset": "SO2", "layers": "SO2", "time": rangeDate },
                tileLoadFunction: layerTileLoad,
                transition: 0,
                crossOrigin: 'anonymous'
            });
            layerOpacity = 0.7;
       }else if(layerName == 'O3'){
            legendUrl = "https://<your layer legend url>";
            newSource =  new ol.source.TileWMS({
                cacheSize: 0,
                url: "https://<your layer service provider url>",
                params: {"maxcc":"100","minZoom":6,"maxZoom":16,"preset":"O3","layers":"O3","time":rangeDate},
                  tileLoadFunction: layerTileLoad,
                  transition: 0,
                  crossOrigin: 'anonymous'
              });
              layerOpacity = 0.7;
       }else if(layerName == 'NO2'){
            legendUrl = "https://<your layer legend url>";
            newSource = new ol.source.TileWMS({
                cacheSize: 0,
                url: "https://<your layer service provider url>",
                params: {"maxcc":100,"minZoom":6,"maxZoom":16,"preset":"NO2","layers":"NO2","time":rangeDate},
                  tileLoadFunction: layerTileLoad,
                  transition: 0,
                  crossOrigin: 'anonymous'
              });
              layerOpacity = 0.7;
       }else if(layerName == 'HCHO'){
            legendUrl = "https://<your layer legend url>";
            newSource =  new ol.source.TileWMS({
                cacheSize: 0,
                url: "https://<your layer service provider url>",
                params: {"maxcc": 100, "minZoom": 6, "maxZoom": 16, "preset": "HCHO", "layers": "HCHO", "time": rangeDate },
                tileLoadFunction: layerTileLoad,
                transition: 0,
                crossOrigin: 'anonymous'
            });
            layerOpacity = 0.7;
       }else if(layerName == 'CO'){
            legendUrl = "https://<your layer legend url>";
            newSource = new ol.source.TileWMS({
                cacheSize: 0,
                url: "https://<your layer service provider url>",
                params: {"maxcc":"100","minZoom":6,"maxZoom":16,"preset":"CO","layers":"CO","time":rangeDate},
                tileLoadFunction: layerTileLoad,
                transition: 0,
                crossOrigin: 'anonymous'
           });
           layerOpacity = 0.7;
       }else if(layerName == 'CH4'){
            legendUrl = "https://<your layer legend url>";
            newSource = new ol.source.TileWMS({
                url: "https://<your layer service provider url>",
                params: {"maxcc": 100, "minZoom": 6, "maxZoom": 16, "preset": "CH4", "layers": "CH4", "time": rangeDate },
                tileLoadFunction: layerTileLoad,
                transition: 0,
                crossOrigin: 'anonymous'
            });
            layerOpacity = 0.7;
       }else if(layerName == 'F2_VISUALIZED'){
            legendUrl = "https://<your layer legend url>";
            newSource = new ol.source.TileWMS({
                cacheSize: 0,
                url: "https://<your layer service provider url>",
                params: { "maxcc": 100, "minZoom": 6, "maxZoom": 16, "preset": "F2_VISUALIZED", "layers": "F2_VISUALIZED", "time": rangeDate },
                tileLoadFunction: layerTileLoad,
                transition: 0,
                crossOrigin: 'anonymous'
            });
            layerOpacity = 0.7;
       }else if(layerName == 'F1_VISUALIZED'){
            legendUrl = "https://<your layer legend url>";
            newSource = new ol.source.TileWMS({
                cacheSize: 0,
                url: "https://<your layer service provider url>",
                params: { "maxcc": 100, "minZoom": 6, "maxZoom": 16, "preset": "F1_VISUALIZED", "layers": "F1_VISUALIZED", "time": rangeDate },
                tileLoadFunction: layerTileLoad,
                transition: 0,
                crossOrigin: 'anonymous'
            });
            layerOpacity = 0.7;
       }

       let tileCount = 0;
       let tileLoadedCount = 0;

        newSource.on("tileloadstart", function(evt){
           if(ISANDROID){
                WebViewBridge.showLoader();
            }else{
                //ios
                window.webkit.messageHandlers.loaderAction.postMessage("true");
            }
            tileCount++;
        });

        newSource.on("tileloaderror", function(evt){
            tileLoadedCount++;
            if(tileLoadedCount == tileCount){
                //evento fine load layer
                console.warn("FINITO CARICAMENTO LAYER CON ERRORI");
                if(ISANDROID){
                    WebViewBridge.hideLoader();
                }else{
                    //ios
                    window.webkit.messageHandlers.loaderAction.postMessage("false");
                }
            }
        });

        newSource.on("tileloadend", function(evt){
            tileLoadedCount++;
            if(tileLoadedCount == tileCount){
                console.log("FINITO CARICAMENTO LAYER");
                if(justStarted){
                    justStarted = false;
                    fadeFirstContext(layerOpacity);
                }else{
                    if(lastLayer != currentLayer || lastRangeDate != rangeDate){
                       if(!wasContextLayer){
                            fadeSwitchToContext(layerOpacity);
                            wasContextLayer = true;
                       }else{
                            fadeContextToSwitch(layerOpacity);
                            wasContextLayer = false;
                       }
                       lastLayer= currentLayer;
                       lastRangeDate = rangeDate;
                    }
                }

                if(ISANDROID){
                    WebViewBridge.hideLoader();
                }else{
                    //ios
                    window.webkit.messageHandlers.loaderAction.postMessage("false");
                }
            }
        });

       if(!justStarted){
            if(lastLayer != currentLayer || lastRangeDate != rangeDate){
                if(!wasContextLayer){
                    contextLayer.setSource(newSource);
                    contextLayer.setOpacity(0);
                    contextLayer.setVisible(true);

               }else{
                    contextSwitchLayer.setSource(newSource);
                    contextSwitchLayer.setOpacity(0);
                    contextSwitchLayer.setVisible(true);

               }
            }

       }else{
            contextLayer.setSource(newSource);
            contextLayer.setOpacity(0);
            lastLayer = currentLayer;
            lastRangeDate = rangeDate;
       }

        if(ISANDROID){
            WebViewBridge.loadLegendImage(legendUrl);
        }else{
            window.webkit.messageHandlers.legendUrl.postMessage(legendUrl);
        }



}
var fadeSwitchToContext = function(targetOpacity){

    let currentOpacity = 0;
    fadeInterval = setInterval(()=>{
        let nextSwitchOpacity = Math.max((contextSwitchLayer.getOpacity() - 0.05), 0);
        contextSwitchLayer.setOpacity(nextSwitchOpacity);
        currentOpacity = Math.min((currentOpacity+0.05), targetOpacity);
        contextLayer.setOpacity(currentOpacity);
        if(nextSwitchOpacity == 0 && currentOpacity == targetOpacity){
            contextSwitchLayer.setVisible(false);
            clearInterval(fadeInterval);
        }
    }, 50);
}
var fadeContextToSwitch = function(targetOpacity){

    let currentOpacity = 0;
    fadeInterval = setInterval(()=>{
        let nextContextOpacity = Math.max((contextLayer.getOpacity() - 0.05), 0);
        contextLayer.setOpacity(nextContextOpacity);
        currentOpacity = Math.min((currentOpacity+0.05), targetOpacity);
        contextSwitchLayer.setOpacity(currentOpacity);
        if(nextContextOpacity == 0 && currentOpacity == targetOpacity){
            contextLayer.setVisible(false);
            clearInterval(fadeInterval);
        }
    }, 50);
}

var fadeFirstContext = function(targetOpacity){
    currentOpacity = 0;
    fadeInterval = setInterval(()=>{
        currentOpacity = Math.min((currentOpacity+0.05), targetOpacity);
        contextLayer.setOpacity(currentOpacity);
        if(currentOpacity == targetOpacity){
            clearInterval(fadeInterval);
        }
    }, 50);
}



var drawSource = new ol.source.Vector({wrapX: false});
var drawLayer;
var selectedFeature;
var isDrawing = false;
var drawingEnabled = false;

function enableDraw(shape){
    drawingEnabled = true;
     if(shape == 'square'){
        drawInteraction = new ol.interaction.Draw({
          source: drawSource,
          type: 'Circle',
          geometryFunction: ol.interaction.Draw.createBox()
        });
     }
     else if (shape == 'circle'){
        drawInteraction = new ol.interaction.Draw({
          source: drawSource,
          type: 'Circle',
        });
     }
     else if (shape == 'triangle'){
        drawInteraction = new ol.interaction.Draw({
          source: drawSource,
          type: 'Circle',
          geometryFunction: ol.interaction.Draw.createRegularPolygon(3)
        });
     }

     drawInteraction.on('drawstart', function(e){
        isDrawing = true;
        drawingOverlay.setVisible(false);
     });
     drawInteraction.on('drawend', function (e) {
        isDrawing = false;
         let currentFeature = e.feature;
         let restOfFeats = drawSource.getFeatures();
         let allFeats = restOfFeats.concat(currentFeature);
         console.log(allFeats.length, allFeats);
     });

     map.addInteraction(drawInteraction);

     drawLayer.setSource(drawSource);
     drawLayer.setStyle(new ol.style.Style({
                     fill: new ol.style.Fill({
                       color: 'rgba(255, 255, 255, 0.5)',
                     }),
                     stroke: new ol.style.Stroke({
                       color: 'rgba(255, 136, 0, 1)',
                       width: 2,
                     }),
                  }));

      map.on(['singleclick'], function(evt) {
        if(!drawingEnabled){
            debugger;
            let features = drawLayer.getSource().getFeaturesAtCoordinate(evt.coordinate);
            if(typeof features !== 'undefined' && features != null && features.length > 0){
                let feature = features[0];
                let geometry = feature.getGeometry();
                if(geometry != null){
                    drawingOverlay.setPosition(geometry.getFirstCoordinate());
                    map.addOverlay(drawingOverlay);
                    drawingOverlay.setVisible(true);
                }
                selectedFeature = feature;
            }else{
                map.removeOverlay(drawingOverlay);
                drawingOverlay.setVisible(false);
            }
        }
      });

}

function disableDraw(){
    drawingEnabled = false;
    map.removeInteraction(drawInteraction);
    map.removeOverlay(drawingOverlay);
}

function deleteFeature(){
    drawLayer.getSource().removeFeature(selectedFeature);
    drawingOverlay.setVisible(false);
    map.removeOverlay(drawingOverlay);
}

function downloadFeatureImage(){
    let bbox = selectedFeature.getGeometry().getExtent().join(",");
    WebViewBridge.setFeatureBoundingBox(bbox);
}

function zoomIn(){
    let nextZoomIn = Math.ceil(map.getView().getZoom() + 1);
    let zoomInVal = nextZoomIn > map.getView().getMaxZoom() ? map.getView().getMaxZoom() : nextZoomIn;

    map.getView().animate({
        center: map.getView().getCenter(),
        duration: 500,
        rotation: 0,
        zoom: zoomInVal
    });
}

function zoomOut(){
    let nextZoomOut = Math.ceil(map.getView().getZoom() - 1);
    let zoomOutVal = nextZoomOut < map.getView().getMinZoom() ? map.getView().getMinZoom() : nextZoomOut;
    map.getView().animate({
        center: map.getView().getCenter(),
        duration: 500,
        rotation: 0,
        zoom: zoomOutVal
    });
}



function degrees_to_radians(degrees) {
  var pi = Math.PI;
  return degrees * (pi / 180);
}

var noDataTile = "data:image/png;base64,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AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA0KX/Dwh+rvyByh2PAAAAAElFTkSuQmCC";
var tileDownloadFailed = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAgAAAAIACAYAAAD0eNT6AAAFtmlUWHRYTUw6Y29tLmFkb2JlLnhtcAAAAAAAPD94cGFja2V0IGJlZ2luPSLvu78iIGlkPSJXNU0wTXBDZWhpSHpyZVN6TlRjemtjOWQiPz4KPHg6eG1wbWV0YSB4bWxuczp4PSJhZG9iZTpuczptZXRhLyIgeDp4bXB0az0iWE1QIENvcmUgNS41LjAiPgogPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4KICA8cmRmOkRlc2NyaXB0aW9uIHJkZjphYm91dD0iIgogICAgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIgogICAgeG1sbnM6cGhvdG9zaG9wPSJodHRwOi8vbnMuYWRvYmUuY29tL3Bob3Rvc2hvcC8xLjAvIgogICAgeG1sbnM6ZGM9Imh0dHA6Ly9wdXJsLm9yZy9kYy9lbGVtZW50cy8xLjEvIgogICAgeG1sbnM6ZXhpZj0iaHR0cDovL25zLmFkb2JlLmNvbS9leGlmLzEuMC8iCiAgICB4bWxuczp0aWZmPSJodHRwOi8vbnMuYWRvYmUuY29tL3RpZmYvMS4wLyIKICAgIHhtbG5zOnhtcE1NPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvbW0vIgogICAgeG1sbnM6c3RFdnQ9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZUV2ZW50IyIKICAgeG1wOkNyZWF0ZURhdGU9IjIwMjMtMTAtMDlUMTc6MjI6MTErMDIwMCIKICAgeG1wOk1vZGlmeURhdGU9IjIwMjMtMTAtMjZUMTg6NTE6MzArMDI6MDAiCiAgIHhtcDpNZXRhZGF0YURhdGU9IjIwMjMtMTAtMjZUMTg6NTE6MzArMDI6MDAiCiAgIHBob3Rvc2hvcDpEYXRlQ3JlYXRlZD0iMjAyMy0xMC0wOVQxNzoyMjoxMSswMjAwIgogICBwaG90b3Nob3A6Q29sb3JNb2RlPSIzIgogICBwaG90b3Nob3A6SUNDUHJvZmlsZT0ic1JHQiBJRUM2MTk2Ni0yLjEi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";
var noDataTileRot = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAgAAAAIACAYAAAD0eNT6AAAFtmlUWHRYTUw6Y29tLmFkb2JlLnhtcAAAAAAAPD94cGFja2V0IGJlZ2luPSLvu78iIGlkPSJXNU0wTXBDZWhpSHpyZVN6TlRjemtjOWQiPz4KPHg6eG1wbWV0YSB4bWxuczp4PSJhZG9iZTpuczptZXRhLyIgeDp4bXB0az0iWE1QIENvcmUgNS41LjAiPgogPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4KICA8cmRmOkRlc2NyaXB0aW9uIHJkZjphYm91dD0iIgogICAgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIgogICAgeG1sbnM6cGhvdG9zaG9wPSJodHRwOi8vbnMuYWRvYmUuY29tL3Bob3Rvc2hvcC8xLjAvIgogICAgeG1sbnM6ZGM9Imh0dHA6Ly9wdXJsLm9yZy9kYy9lbGVtZW50cy8xLjEvIgogICAgeG1sbnM6ZXhpZj0iaHR0cDovL25zLmFkb2JlLmNvbS9leGlmLzEuMC8iCiAgICB4bWxuczp0aWZmPSJodHRwOi8vbnMuYWRvYmUuY29tL3RpZmYvMS4wLyIKICAgIHhtbG5zOnhtcE1NPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvbW0vIgogICAgeG1sbnM6c3RFdnQ9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZUV2ZW50IyIKICAgeG1wOkNyZWF0ZURhdGU9IjIwMjMtMTAtMDlUMTc6MjI6MTErMDIwMCIKICAgeG1wOk1vZGlmeURhdGU9IjIwMjMtMTAtMDlUMTk6MDI6NDkrMDI6MDAiCiAgIHhtcDpNZXRhZGF0YURhdGU9IjIwMjMtMTAtMDlUMTk6MDI6NDkrMDI6MDAiCiAgIHBob3Rvc2hvcDpEYXRlQ3JlYXRlZD0iMjAyMy0xMC0wOVQxNzoyMjoxMSswMjAwIgogICBwaG90b3Nob3A6Q29sb3JNb2RlPSIzIgogICBwaG90b3Nob3A6SUNDUHJvZmlsZT0ic1JHQiBJRUM2MTk2Ni0yLjEi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iXS+F4+0bG6eEfA4YNsOTf5NFAG6CLhSQX8i0oQGACI9MrPjgYOSw3u7+xmjfJ13AasD8xT+uwu4tGb5YRGRNhoAiPQo3/73ELETYMizwBL5jgERkb6lbYAiPXL354GDk8PzEPkCRET6mgYAIiNzETEdX/RpM1u2rLGISL/QAEBkBPJAvz2Tw9MDp3VK3iMi0g80ABAZIXe/A7g4ObwesMk43I6ISC0aAIiMjs/Snsb3VDObuayxiMh40wBAZBS4+x+Igj5Fi9O+PCAi0hc0ABAZPScDTyTHPmdmC4zHzYiIVNEAQGSUuPs/gf2Tw3MShYBERPqKBgAio+vbwK3JsZ3NLBuPmxER6UTblERGmZmtDLQY/vN1G/Bed586PnclIjKcqgGKjLJWq/VUlmULAUNv/S1gf3dP4wNERMaNlgBEpo3DiDoBOwOrufvt43w/IiIiMhbMTANsEREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREREZM/8PTOa1ts208rIAAAAASUVORK5CYII=";

var layerTileLoad = async function(imageTile, src) {

    let coords = imageTile.getTileCoord();
    let zoom = coords[0];
    let x = coords[1];
    let y = coords[2];
    let image = imageTile.getImage();
    let strCoords = coords.join("/");

    let srcSplit = src.split("&");
    let layerName = "";
    let dateRange = "";

    layerName = srcSplit.find((element) => element.startsWith("layers="));
    dateRange = srcSplit.find((element) => element.startsWith("time="));

    let tile;
    if((typeof layerName !== 'undefined' && layerName != null && layerName.length > 0)&&
        (typeof dateRange !== 'undefined' && dateRange != null && dateRange.length > 0) && ISANDROID){
        // caching disabled
        //tile = await db.layers.where({ "coord": strCoords, "layer": layerName.substr(7), "range": dateRange.substr(5) }).first();
     }

    if(typeof tile !== 'undefined'){
        console.log("Load Layer from DB "+strCoords);
        image.src = await URL.createObjectURL(tile.image);//await blobToBase64(tile.image);
    }else{
        //console.log("Not found on db "+strCoords+" - " +layerName.substr(7)+ " - "+ dateRange.substr(5));
        try{
            const res = await fetch(src);
            const b = await res.blob();

            const isBlank = await checkIfImageIsBlank_pixelopt(b);
            if(isBlank){
                image.src = noDataTileRot;
            }else{
                image.src = URL.createObjectURL(b);
                // caching disabled
                /*if(ISANDROID)
                    await db.layers.put({ "layer":layerName.substr(7), "coord": strCoords, "image": b, "range":dateRange.substr(5) })*/
            }

        }catch(e){
            console.error(e.stack);
            imageTile.getImage().src = tileDownloadFailed;
            imageTile.setState(3);
        }
    }

};

var checkIfImageIsBlank_pixelopt = async function(blob){
    let isBlank = true;
    let canvas = document.createElement('canvas');
    ctx = canvas.getContext("2d", { willReadFrequently: true });

    let fullimg = await loadImageForCheck(blob);
    isBlank = await resizeImageWithCheck(fullimg);
    return isBlank;
}

var loadImageForCheck = function(blob) {
  return new Promise(r => {
    let i = new Image();
    i.onload = (() => r(i));
    i.src = URL.createObjectURL(blob);
  });
}

var resizeImageWithCheck = function(img){
    return new Promise(r => {

        let canvas = document.createElement("canvas");
        let ctx = canvas.getContext("2d", { willReadFrequently: true });
        canvas.width = 5;
        canvas.height = 5;
        // Actual resizing
        ctx.drawImage(img, 0, 0, canvas.width, canvas.height);

        // Show resized image in preview element
        let dataUrl = canvas.toDataURL(img.type);

        let i = new Image();
        i.onload = (() =>{
            let isBlank = true;
            dance:
            for(let x = 0; x < i.width; x++){
                for(let y= 0; y < i.height; y++){
                    let pixel = ctx.getImageData(x, y, 1, 1);
                    let data = pixel.data;
                    if((data[0] != 0 || data[1] != 0 || data[2] != 0 || data[3] != 0)){
                        isBlank = false;
                        break dance;
                    }
                }
            }
            r(isBlank);
        });
        i.src = dataUrl;
    });
}

var tileLoadCache = async function (imageTile, src){

    let coords = imageTile.getTileCoord();

    let zoom = coords[0];
    let x = coords[1];
    let y = coords[2];
    let image = imageTile.getImage();
    let strCoords = coords.join("/");

    let tile;
    if(ISANDROID)
        tile = await db.tiles.where("coord").equals(strCoords).first();
    if(typeof tile !== 'undefined'){
        //const tile = await db.tiles.where("coord").equals(strCoords).first();
        //console.log("Load OSM from DB "+strCoords);
        image.crossOrigin = "anonymous";
        image.src = URL.createObjectURL(tile.image);
    }else{
        try{
            const res = await fetch(src);
            const b = await res.blob();
            /*const base64data = await blobToBase64(b);
            image.src = base64data;*/
            image.src = URL.createObjectURL(b);
            if(ISANDROID)
                await db.tiles.put({ "coord": strCoords, "image": b })
        }catch(e){
            image.src = tileDownloadFailed;
            imageTile.setState(3); //ERROR
        }
    }
}