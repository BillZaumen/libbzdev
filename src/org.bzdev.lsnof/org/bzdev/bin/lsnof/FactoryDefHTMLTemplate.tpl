<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
        "http://www.w3.org/TR/html4/strict.dtd">
<HTML>
  <HEAD>
    <META HTTP-EQUIV="content-type" content="text/html; charset=UTF-8">
    <TITLE>Factory Parameter Defining Classes</TITLE>
  </HEAD>
  <BODY>$(factories:endFactories)
    <TABLE border="1">
      <THEAD>
	<TR><TH>Factory:</TH><TH colspan=4>$(factory)</TH></TR>
	<TR>
	  <TH>Parameter</TH>
	  <TH>Defining Class</TH>
	</TR>
      </THEAD>
      <TBODY>$(parameters:endParameters)
	<TR>
	  <TD>$(name)</TD>
	  <TD>$(definingFactoryClass)</TD>
	</TR>$(endParameters)
      </TBODY>
    </TABLE>$(endFactories)
  </BODY>
</HTML>
