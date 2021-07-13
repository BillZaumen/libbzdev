scripting.importClass("org.bzdev.math.RealValuedFunctionThree");

function f(x,y,z)   {return Math.sin(x) * Math.cos(y) * z;}
function f1(x,y,z)  {return Math.cos(x) * Math.cos(y) * z;}
function f2(x,y,z) {return -Math.sin(x) * Math.sin(y) * z;}
function f3(x,y,z)   {return Math.sin(x) * Math.cos(y);}
function f11(x,y,z) {return -Math.sin(x) *  Math.cos(y) * z;}
function f12(x,y,z) {return -Math.cos(x) * Math.sin(y) * z;}
function f13(x,y,z)  {return Math.cos(x) * Math.cos(y);}
function f21(x,y,z) {return -Math.cos(x) * Math.sin(y) * z;}
function f22(x,y,z) {return -Math.sin(x) * Math.cos(y) * z;}
function f23(x,y,z) {return -Math.sin(x) * Math.sin(y);}
function f31(x,y,z)  {return Math.cos(x) * Math.cos(y);}
function f32(x,y,z) {return -Math.sin(x) * Math.sin(y);}
function f33(x,y,z)   {return 0.0}

new RealValuedFunctionThree(scripting, {
    valueAt: function(x,y,z) {return Math.sin(x) * Math.cos(y) * z;},
    deriv1At: function(x,y,z) {return Math.cos(x) * Math.cos(y) * z;},
    deriv2At: function(x,y,z) {return -Math.sin(x) * Math.sin(y) * z;},
    deriv3At: function(x,y,z) {return Math.sin(x) * Math.cos(y);},
    deriv11At: function(x,y,z) {return -Math.sin(x) *  Math.cos(y) * z;},
    deriv12At: function(x,y,z) {return -Math.cos(x) * Math.sin(y) * z;},
    deriv13At: function(x,y,z) {return Math.cos(x) * Math.cos(y);},
    deriv21At: function(x,y,z) {return -Math.cos(x) * Math.sin(y) * z;},
    deriv22At: function(x,y,z) {return -Math.sin(x) * Math.cos(y) * z;},
    deriv23At: function(x,y,z) {return -Math.sin(x) * Math.sin(y);},
    deriv31At: function(x,y,z) {return Math.cos(x) * Math.cos(y);},
    deriv32At: function(x,y,z) {return -Math.sin(x) * Math.sin(y);},
    deriv33At: function(x,y,z) {return 0.0},
});
