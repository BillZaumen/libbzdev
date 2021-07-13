<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
	  "http://www.w3.org/TR/html4/strict.dtd">
<HTML>
<HEAD>
<META http-equiv="Content-Type" content="text/html; charset=$(encoding)">
<TITLE>Directory for $(dirname)</TITLE>
</HEAD>
<BODY  style="background-color: lightgray">
<b>$(dirname) directory listing:</b>
<ul>$(items:endItems)
<li><a href="$(href)">$(isDirectory:endDir)<B>$(endDir)$(entry)$(isDirectory:endDir)</B>$(endDir)</a>$(endItems)
</ul>
</BODY>
</HTML>
