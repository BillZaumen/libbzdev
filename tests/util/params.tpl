var hasAllImages = $(hasAllImages);
var imageTime = $(imageTime);
var minImageTime = $(minImageTime);
var syncMode = $(syncMode);
var waitOnError = $(waitOnError);
var highResDir = "$(highResDir)";
var wOffset =  $(wOffset);
var hOffset =  $(hOffset);
var wPercent = $(wPercent);
var hPercent = $(hPercent);

// Entries to domMap are index by a key giving the name of a property
// for an element of the imageArray array. The keys title, descr,
// name, width, height, and ext are reserved.
// The values are objects containing a the following properties:
//   id - the ID attribute of an HTML element to modify. We assume the
//   following IDs exist:
//         title, descr, img
//   prop - the element-object property to modify
//   defaultValue - the default value to use a key does not exist for
//       the current element of imageArray.
// For example,
// var domMap = {key1: {id: "button", prop: "disabled", defaultValue: "true"}}
// An entry in imageArray can then set the property key1 to false to disable
// the HTML object whose id is "button", and this will occur only for that
// entry.
// var domMap = {};


var imageArray = 
    [
$(repeatImageArrayEntries:endImageArray)     {name: "$(name)", width: $(width), height: $(height), ext: "$(ext)", highImageURL: "$(highImageURL)", fsImageURL: "$(fsImageURL)", hrefURL: "$(hrefURL)", hrefTarget: "$(hrefTarget)" $(otherProps)}$(commaSeparator)
$(endImageArray)    ];

var imageEntryReference = {entry: null};

var domMap = {$(repeatDomEntries:endDomEntries)$(domKey): {mode: $(domModeCode) $(domIDsInsert)$(domPropInsert)$(domDefaultValueInsert)$(domCallModeInsert)$(domMethodInsert)$(domFunctionInsert)$(domDefaultArgumentInsert)}$(commaSeparator)$(endDomEntries)};

lowx1 test (lowx1 set to true):
$(+lowx1:endLow)
x = $(x)  [+lowx1]
$(endLow)
$(-lowx1:endLow)
x = $(x) [-lowx1]
$(endLow)

lowx2 test (lowx2 set to null):
$(+lowx2:endLow)
x = $(x) [+lowx2]
$(endLow)
$(-lowx2:endLow)
x = $(x) [-lowx2]
$(endLow)
