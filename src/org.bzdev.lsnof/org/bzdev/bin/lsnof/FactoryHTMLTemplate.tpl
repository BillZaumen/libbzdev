<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
        "http://www.w3.org/TR/html4/strict.dtd">
<HTML>
  <HEAD>
    <META HTTP-EQUIV="content-type" content="text/html; charset=UTF-8">
    <TITLE>Factory Parameter Documentation</TITLE>
  </HEAD>
  <BODY>$(factories:endFactories)
    <TABLE border="1">
      <THEAD>
	<TR><TH>Factory:</TH><TH colspan=4>$(factoryAPI)</TH></TR>
	<TR>
	  <TH>Parameter</TH>
	  <TH>Types<BR>(optional keytype)<BR>(value type)</TH>
	  <TH>Range</TH>
	  <TH>RV Mode</TH>
	  <TH>Add/Set</TH>
      </THEAD>
      <TBODY>$(parameters:endParameters)
	<TR>
	  <TD rowspan="$(hasDoc:endDoc)3$(endDoc)$(noDoc:endDoc)2$(endDoc)"
	      valign="top">
	    <TABLE border="0">
      <TR><TD><CODE><B><A name="$(name)-$(factory)">$(name)</A></B></CODE>
	      </TD></TR>
	      <TR><TD><I>$(label)</I></TD></TR>
	    </TABLE>
	  </TD>
	  <TD>$(types)</TD>
	  <TD>$(range)</TD>
	  <TD>$(rvmode)</TD>
	  <TD>$(isAddable)</TD>
	</TR>
	<TR><TD colspan="4">$(descriptionHTML)</TD></TR>$(hasDoc:endDoc)
	<TR><TD colspan="4"><DIV>$(doc)</DIV></TD></TR>$(endDoc)$(endParameters)
      </TBODY>
    </TABLE>$(endFactories)
  </BODY>
</HTML>
