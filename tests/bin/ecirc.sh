#!/bin/sh
./run -L ESP -vD:a:$1 -vD:b:$2 - <<"EOF"
import (java.lang.Math);
import (org.bzdev.math.Functions);
var a;
var b;
function circE (a, b) {
    var e2 = (a*a - b*b)/(a*a);
    (e2 < 0)? circE(b, a): 4*a*eE(sqrt(e2))
}
global.getWriter().println(circE(a,b));
EOF
