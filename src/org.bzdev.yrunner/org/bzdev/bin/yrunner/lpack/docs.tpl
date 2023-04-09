<!DOCTYPE HTML>
<HTML lang="en">
  <BODY>
    <H2>API Documentation for yrunner $(lnames)</H2>
    <P>
    With the launcher and add-ons specified by <STRONG>$(lnames)</STRONG>,
    only specific Java classes may be used. These are grouped into several
    sections:
    <UL>
      <LI><A HREF="#retvals">Return Values</A>. This section lists the
	classes whose constructors can be used and that can be the
	values of functions or methods.
      <LI><A HREF="#args">Arguments</A>. This section lists the
	classes that can be used as arguments for functions or
	methods.
      <LI><A HREF="#constrs">Constructors</A>. This section lists
	constructors.
      <LI><A HREF="#functs">Functions</A>. This section lists functions
	(static methods that are used as functions).
      <LI><A HREF="#methods">Methods</A>. This section lists instance
	methods.
      <LI><A HREF="#consts">Constants</A>. This section lists constants,
	including enumerations.
    </UL>
    Each section contains links to the API documentation for the corresponding
    classes, methods, and fields.

    <H2><A ID="retvals">Return-Value Classes</A></H2>
    <P>
      $(retList:endList)<A HREF="$(href)">$(item)</A>
$(endList)

    <H2><A ID="args">Argument Classes</A></H2>
    <P>
      $(argList:endList)<A HREF="$(href)">$(item)</A>
$(endList)

    <H2><A ID="constrs">Constructors</A></H2>
    <P>
      Constructors are available for the following classes:$(constrClasses:endCC)
      <A HREF="#constr-$(class)">$(class)</A>$(endCC).
    <P>
      The constructors are are shown with links to the API documentation:
    <UL>
      $(constrList:endList)<LI>$(+title:eT)<A ID="constr-$(class)">$(eT)<A HREF="$(href)">$(class)</A>$(+title:eT)</A>$(eT)$(arguments)
$(endList)
    </UL>

    <H2><A ID="functs">Functions</A></H2>
    <P>
      Functions are provided by the following classes:$(functClasses:endF)
      <A HREF="#funct-$(class)">$(class)</A>$(endF).
    <P>
      The functions are shown with links to the API documentation:
    <UL>
      $(functList:endList)<LI>class $(+title:eT)<A ID="funct-$(class)">$(eT)$(class)$(+title:eT)</A>$(eT) &mdash; <A HREF="$(href)">$(method)</A>$(arguments)
$(endList)
    </UL>

    <H2><A ID="methods">Methods</A></H2>
    <P>
      Instance methods are provided by the following classes:$(methodClasses:endM)
      <A HREF="#method-$(class)">$(class)</A>$(endM)
    <P>
      The methods are shown with links to the API documentation:
    <UL>
      $(methodList:endList)<LI>class $(+title:eT)<A ID="method-$(class)">$(eT)$(class)$(+title:eT)</A>$(eT) &mdash; <A HREF="$(href)">$(method)</A>$(arguments)
$(endList)
    </UL>

    <H2><A ID="consts">Constants</A></H2>
    <P>
      $(constList:endList)<A HREF="$(href)">$(item)</A>
$(endList)

  </BODY>
</HTML>
