scripting.importClass("org.bzdev.math.RealValuedFunction");

function f(x)   {return Math.sin(x);}
function fp(x)  {return Math.cos(x);}
function fpp(x) {return -Math.sin(x);}

new RealValuedFunction(scripting, {
    valueAt: function(x) {return Math.sin(x);},
    derivAt: function(x) {return Math.cos(x);},
    secondDerivAt: function(x) {return -Math.sin(x);}
});
