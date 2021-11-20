<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
        "http://www.w3.org/TR/html4/strict.dtd">
<HTML>
  <HEAD>
    <META HTTP-EQUIV="content-type" content="text/html; charset=UTF-8">
    <META HTTP-EQUIV="Content-Style-Type" ocntent="text/css">
    <STYLE type="text/css">
    * { margin: 0; padding: 0;
    }
    h2 {
       font-weight:bold;
       font-style: italic;
       font-size:16px;
       padding:10px 0 0 0;
    }
    ul {
       margin:0;
    }
    ul li {
       list-style:none;
    }
    body {
	  background-color:$(menuBackground);
	  color:$(menuColor);
      }
      a:link, a:visited {
	  text-decoration:none;
	  color:$(menuLinkColor)
      }
    </STYLE>
    <TITLE>Factories</TITLE>
  </HEAD>
  <BODY>
    <h2><A HREF="overview.html" target="factories">Factories</A></h2>
    <ul>$(factories:endFactories)
      $(nextPackageEntry)<li>$(factoryDoc)
    $(endFactories)</ul>
  </BODY>
</HTML>
