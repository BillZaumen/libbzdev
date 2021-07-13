scripting.importClass("org.bzdev.math.RealValuedFunctionVA");

function f(args) {
    var x = args[0];
    var y = args[1];
    return Math.sin(x) * Math.cos(y);
}

function fp(i, args)  {
    var x = args[0];
    var y = args[1];
    if (i == 0) {
	return Math.cos(x) * Math.cos(y);
    } else if (i == 1) {
	return -Math.sin(x) * Math.cos(y);
    }
}

function fpp(i, j, args) {
    var x = args[0];
    var y = args[1];
    if (i == 0 && j == 0) {
	return -Math.sin(x) *  Math.cos(y);
    } else if (i == 0 && j == 1) {
	return -Math.cos(x) * Math.sin(y);
    } else if (i == 1 && j == 0) {
	return -Math.cos(x) * Math.sin(y);
    } else if (i == 1 && j == 1) {
	return -Math.sin(x) * Math.cos(y);
    }
}

new RealValuedFunctionVA(2,2, scripting, {
    valueAt: function(args) {
	var x = args[0];
	var y = args[1];
	return Math.sin(x) * Math.cos(y);
    },
    derivAt: function(i,args) {
	var x = args[0];
	var y = args[1];
	if (i == 0) {
	    return Math.cos(x) * Math.cos(y);
	} else if (i == 1) {
	    return -Math.sin(x) * Math.cos(y);
	}
	throw Error("Illegal Argument");
    },
    secondDerivAt: function(i,j,args) {
	var x = args[0];
	var y = args[1];
	if (i == 0 && j == 0) {
	    return -Math.sin(x) *  Math.cos(y);
	} else if (i == 0 && j == 1) {
	    return -Math.cos(x) * Math.sin(y);
	} else if (i == 1 && j == 0) {
	    return -Math.cos(x) * Math.sin(y);
	} else if (i == 1 && j == 1) {
	    return -Math.sin(x) * Math.cos(y);
	}
	throw Error("Illegal Argument");
    },
});
