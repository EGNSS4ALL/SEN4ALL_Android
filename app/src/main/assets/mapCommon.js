var db;

function getExtentFromFeatures(features){
    if (features.length > 0){
        var extent_trovata = features[0].getGeometry().getExtent();
        features.forEach(function(feat){
            var curr_ext = feat.getGeometry().getExtent();
            extent_trovata = [Math.min(extent_trovata[0], curr_ext[0]),
                              Math.min(extent_trovata[1], curr_ext[1]),
                              Math.max(extent_trovata[2], curr_ext[2]),
                              Math.max(extent_trovata[3], curr_ext[3])];
        });
        return extent_trovata;
    } else {
        return undefined;
    }
}

function fitMapToLayerExtent(){
    let extent = getExtentFromFeatures(map.getLayers().getArray()[1].getSource().getFeatures());
    map.getView().fit(extent,{duration:1000});
    map.getLayers().getArray()[1].getSource().refresh();
    map.renderSync();
}

function GetURLParameter(sParam)
{
    var sPageURL = window.location.search.substring(1);
    var sURLVariables = sPageURL.split('&');
    for (var i = 0; i < sURLVariables.length; i++)
    {
        var sParameterName = sURLVariables[i].split('=');
        if (sParameterName[0] == sParam)
        {
            return sParameterName[1];
        }
    }
}

function removeFeatures(){
    map.getLayers().getArray()[1].getSource().getFeatures().forEach((f)=>{
        map.getLayers().getArray()[1].getSource().removeFeature(f);
    })
    map.getLayers().getArray()[2].getSource().getFeatures().forEach((f)=>{
        map.getLayers().getArray()[2].getSource().removeFeature(f);
    })
    firstLoc = undefined;
}

function blobToBase64(blob) {
  return new Promise((resolve, _) => {
    const reader = new FileReader();
    reader.onloadend = () => resolve(reader.result);
    reader.readAsDataURL(blob);
  });
}



function initDb(){
    db = new Dexie("SEN4ALLIDXDB");
        db.version(3).stores({
          tiles: "++id, *coord",
          layers: "++id, [layer+coord+range]"
        });
        db.open().catch(function (err) {
            console.error (err.stack || err);
        });
}