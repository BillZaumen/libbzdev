scripting.importClass("org.bzdev.math.RealValuedFunctionTwo");

function f(x,y)   {return Math.sin(x) * Math.cos(y);}
function f1(x,y)  {return Math.cos(x) * Math.cos(y);}
function f2(x, y) {return -Math.sin(x) * Math.sin(y);}
function f11(x,y) {return -Math.sin(x) *  Math.cos(y);}
function f12(x,y) {return -Math.cos(x) * Math.sin(y);}
function f21(x,y) {return -Math.cos(x) * Math.sin(y);}
function f22(x,y) {return -Math.sin(x) * Math.cos(y);}

new RealValuedFunctionTwo(scripting, {
    valueAt: function(x,y) {return Math.sin(x) * Math.cos(y);},
    deriv1At: function(x,y) {return Math.cos(x) * Math.cos(y);},
    deriv2At: function(x,y) {return -Math.sin(x) * Math.sin(y);},
    deriv11At: function(x,y) {return -Math.sin(x) *  Math.cos(y);},
    deriv12At: function(x,y) {return -Math.cos(x) * Math.sin(y);},
    deriv21At: function(x,y) {return -Math.cos(x) * Math.sin(y);},
    deriv22At: function(x,y) {return -Math.sin(x) * Math.cos(y);}
});
