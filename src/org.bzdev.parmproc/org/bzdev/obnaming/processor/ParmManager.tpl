$(package)
import org.bzdev.obnaming.ParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.NamedObjectFactory;
import org.bzdev.obnaming.ParmKeyType;
// import javax.annotation.processing.Generated;

//@exbundle org.bzdev.obnaming.lpack.ParmParser

// @Generated(value="$(generator)", date="$(date)")
class $(manager)$(formalParms) extends ParmManager<$(factory)$(formalParmArgs)> {
     class KeyedTightener {
       $(parmLoop:endLoop0)
         $(compoundCase:endCompoundCase0)
	  void $(valueVar)($(valueType) field) {
	    $(nestedParmLoop:endNestedParmLoop0)
	       $(primCase:endPrimCase0)
	        try {
		  $(keyedTightenMinStatement)
		  $(keyedTightenMaxStatement)
		} catch (NullPointerException e) {}
	       $(endPrimCase0)
	       $(primCaseClone:endPrimCaseClone0)
	        try {
		  $(keyedTightenMinStatement)
		  $(keyedTightenMaxStatement)
		} catch (NullPointerException e) {}
	       $(endPrimCaseClone0)
	    $(endNestedParmLoop0)
	  }
	 $(endCompoundCase0)
         $(keyedCase:endKeyedCase0)
	   void $(mapVar)($(mapValueType) field) {
	     $(nestedParmLoop:endNestedParmLoop0)
	       $(primCase:endPrimCase0)
		try {
		  $(keyedTightenMinStatement)
		  $(keyedTightenMaxStatement)
		} catch (NullPointerException e) {}
	       $(endPrimCase0)
	       $(primCaseClone:endPrimCaseClone0)
		try {
		  $(keyedTightenMinStatement)
		  $(keyedTightenMaxStatement)
		} catch (NullPointerException e) {}
	       $(endPrimCaseClone0)

	     $(endNestedParmLoop0)
	   }
	 $(endKeyedCase0)
	 $(keyedPrimitiveCase:endKeyedPrimitiveCase0)
	  $(endKeyedPrimitiveCase0)
       $(endLoop0)
     }
     KeyedTightener keyedTightener = new KeyedTightener();

     class Defaults {
       $(parmLoop:endLoop1)
         $(primCase:endPrimCase1)
           $(varType) $(varName);
         $(endPrimCase1)
         $(primCaseClone:endPrimCaseClone1)
           $(varType) $(varName);
         $(endPrimCaseClone1)
	 $(compoundCase:endCompoundCase1)
	   $(valueType) $(valueVar);
	 $(endCompoundCase1)
	 $(keyedCase:endKeyedCase1)
	   $(mapValueType) $(mapVar);
	 $(endKeyedCase1)
       $(endLoop1)
    }
    Defaults defaults = new Defaults();

    @SuppressWarnings("unchecked")
    private void initDefaults($(factory)$(formalParmArgs) factory) {
       $(parmLoop:endLoop2)
          $(primCase:endPrimCase2)
            this.defaults.$(varName) = factory.$(varName);
	    try {
	      $(tightenMinStatement)
	      $(tightenMaxStatement)
	    } catch (NullPointerException e){}
	  $(endPrimCase2)
          $(primCaseClone:endPrimCaseClone2)
            this.defaults.$(varName) = factory.$(varName);
	    try {
	      $(tightenMinStatement)
	      $(tightenMaxStatement)
	    } catch (NullPointerException e){}
	    try {
	      Object object = (this.defaults.$(varName) == null)? null:
		this.defaults.$(varName).clone();
	      if (object == null) {
		factory.$(varName) = null;
	      } else if (object instanceof $(erasedVarType)) {
	        factory.$(varName) = ($(varType)) object;
	      }
	    } catch (CloneNotSupportedException e) {}
	  $(endPrimCaseClone2)
	 $(compoundCase:endCompoundCase2)
	   // this.defaults.$(valueVar) = new $(valueType)();
	   try {
		this.defaults.$(valueVar) = (factory.$(valueVar) == null)? null:
		    factory.$(valueVar).getClass().getDeclaredConstructor()
			.newInstance();
	   } catch (Exception e) {
		this.defaults.$(valueVar) = new $(valueType)();
	   }
	   keyedTightener.$(valueVar)(this.defaults.$(valueVar));
	 $(endCompoundCase2)
	 $(keyedCase:endKeyedCase2)
	   this.defaults.$(mapVar) = new $(mapValueType)();
	   keyedTightener.$(mapVar)(this.defaults.$(mapVar));
	 $(endKeyedCase2)
       $(endLoop2)
    }

    @SuppressWarnings("unchecked")
    protected void setDefaults($(factory)$(formalParmArgs) factory) {
       $(parmLoop:endLoop3)
         $(primCase:endPrimCase3)
           if(factory.containsParm("$(parmName)")) {
	        factory.$(varName) = this.defaults.$(varName);
	   }
         $(endPrimCase3)
         $(primCaseClone:endPrimCaseClone3)
           if(factory.containsParm("$(parmName)")) {
	      try {
		Object object = (this.defaults.$(varName) == null)? null:
		  this.defaults.$(varName).clone();
		if (object == null) {
		    factory.$(varName) = null;
		} else if (object instanceof $(erasedVarType)) {
		    factory.$(varName) = ($(varType)) object;
		} else {
		    factory.$(varName) = this.defaults.$(varName);
		}
	      } catch (CloneNotSupportedException e) {
	       factory.$(varName) = this.defaults.$(varName);
	      }
	   }
         $(endPrimCaseClone3)
	 $(primCaseSet:endPrimCaseSet3)
	   factory.$(varName).clear();
	 $(endPrimCaseSet3)
	 $(compoundCase:endCompoundCase3)
	  $(nestedParmLoop:endNestedParmLoop3)
           $(primCase:endPrimCase3)
             if(factory.containsParm("$(keyPrefix)$(keyDelimiter)$(parmName)")){
	         factory.$(valueVar).$(varName) =
		    this.defaults.$(valueVar).$(varName);
	     }
           $(endPrimCase3)
           $(primCaseClone:endPrimCaseClone3)
             if(factory.containsParm("$(keyPrefix)$(keyDelimiter)$(parmName)")){
	        try {
		  Object object =
		    (this.defaults.$(valueVar).$(varName) == null)? null:
		    this.defaults.$(valueVar).$(varName).clone();
		  if (object == null) {
		     factory.$(valueVar).$(varName) = null;
		  } else if (object instanceof $(erasedVarType)) {
		     factory.$(valueVar).$(varName) = ($(varType)) object;
		  } else {
		     factory.$(valueVar).$(varName) =
		          this.defaults.$(valueVar).$(varName);
		  }
	        } catch (CloneNotSupportedException e) {
	          factory.$(valueVar).$(varName) =
		      this.defaults.$(valueVar).$(varName);
	        }
	     }
           $(endPrimCaseClone3)
	   $(primCaseSet:endPrimCaseSet3)
	     factory.$(valueVar).$(varName).clear();
	   $(endPrimCaseSet3)
	  $(endNestedParmLoop3)
	 $(endCompoundCase3)
	 $(keyedCase:endKeyedCase3)
	   factory.$(mapVar).clear();
	 $(endKeyedCase3)
	 $(keyedPrimitiveCase:endKeyedPrimitiveCase3)
	   factory.$(mapVar).clear();
	 $(endKeyedPrimitiveCase3)
       $(endLoop3)
    }

    $(manager)(final $(factory)$(formalParmArgs) factory) {
        super(factory);
	initDefaults(factory);
	Class<?>[] carray = null;
	ParmKeyType qname = null;
	$(hasTip:endHasTip)
	  addTipResourceBundle("$(tipBaseName)", $(manager).class);
	$(endHasTip)
	$(hasDoc:endHasDoc)
	  addDocResourceBundle("$(docBaseName)", $(manager).class);
	$(endHasDoc)
	$(hasLabel:endHasLabel)
	  addLabelResourceBundle("$(labelBaseName)", $(manager).class);
	$(endHasLabel)
	$(parmLoop:endLoop4)
	    $(primCase:endPrimCase4)
	       addParm(new Parm("$(parmName)", $(rvClass),
	               new ParmParser() {
		        $(parse:endParse1)
			 @SuppressWarnings("unchecked")
			 public void parse($(baseParmType) value) {
			     if ($(baseParmTest)) {
				factory.$(varName) = ($(varType))value;
			     } else {
			        throw new IllegalArgumentException
				  (errorMsg("wrongType1", getParmName()));
			     }
			 }
			$(endParse1)$(noSuppress:endNoSuppress1)
			  @SuppressWarnings("unchecked")
		          public void parse($(parmType) value) {
		               factory.$(varName) = $(startCall)value$(endCall);
		          }$(endNoSuppress1)
		          public void clear() {
		              factory.$(varName) = defaults.$(varName);
		          }
		       },
		       $(type).class,
		       $(glbTerm), $(glbClosed),
		       $(lubTerm), $(lubClosed)));
            $(endPrimCase4)
	    $(primCaseClone:endPrimCaseClone4)
	       // primitive cloned case
	       addParm(new Parm("$(parmName)", $(rvClass),
	               new ParmParser() {
			  @SuppressWarnings("unchecked")
		          public void parse($(parmType) value) {
		             if (value instanceof $(erasedVarType))
		               factory.$(varName) = value;
			     else
			       throw new IllegalArgumentException
				  (errorMsg("wrongType1", getParmName()));
		          }
			  @SuppressWarnings("unchecked")
		          public void clear() {
			     try {
				Object object = (defaults.$(varName) == null)?
				  null: defaults.$(varName).clone();
			        if (object == null) {
				    factory.$(varName) = null;
				} else if (object instanceof $(erasedVarType)) {
				    factory.$(varName) = ($(varType)) object;
				} else {
				    factory.$(varName) = defaults.$(varName);
				}
			     } catch(CloneNotSupportedException e) {
		                factory.$(varName) = defaults.$(varName);
			     }
		          }
		       },
		       $(type).class,
		       $(glbTerm), $(glbClosed),
		       $(lubTerm), $(lubClosed)));
            $(endPrimCaseClone4)
	    $(primCaseSet:endPrimCaseSet4)
	       addParm(new Parm("$(parmName)", $(setofType).class, null,
	               new ParmParser() {
		        $(parse:endParse2)
			 @SuppressWarnings("unchecked")
			 public void parse($(baseParmType) value) {
			     if ($(baseParmTest)) {
				factory.$(varName).add($(baseParmConv)(value));
			     } else {
				throw new IllegalArgumentException
				    (errorMsg("wrongType1", getParmName()));
			     }
			 }
			$(endParse2)$(noSuppress:endNoSuppress2)
			 @SuppressWarnings("unchecked")
		         public void parse($(parmType) value) {
			   factory.$(varName).add($(startCall)value$(endCall));
			 }$(endNoSuppress2)
			 public void clear() {
			   factory.$(varName).clear();
			 }
			 $(parse:endParse3)
			 public void clear($(baseParmType) value) {
			   if ($(baseParmTest)) {
			     factory.$(varName).remove(value);
			   }
			 }
			 $(endParse3)$(noSuppress:endNoSuppress3)
			 public void clear($(parmType) value) {
			   factory.$(varName)
				.remove($(startCall)value$(endCall));
			 }$(endNoSuppress3)
		       },
		       null,
		       null, true, null, true));
	    $(endPrimCaseSet4)
	 $(compoundCase:endCompoundCase4)
	   $(hasTipCPT:endHasTipKeyed)
	       addTipResourceBundle("$(keyPrefix)", "$(keyDelimiter)",
				    "$(tipBaseName)", $(valueType).class);
	   $(endHasTipKeyed)
	   $(hasDocCPT:endHasDocKeyed)
	       addDocResourceBundle("$(keyPrefix)", "$(keyDelimiter)",
				    "$(docBaseName)", $(valueType).class);
	   $(endHasDocKeyed)
	   $(hasLabelCPT:endHasLabelKeyed)
	       addLabelResourceBundle("$(keyPrefix)", "$(keyDelimiter)",
				      "$(labelBaseName)",
				      $(valueType).class);
	   $(endHasLabelKeyed)
	   // add a Parm that just clears a compound parameter (without a key)
	   addParm(new Parm("$(keyPrefix)",
	           new ParmParser() {
		      public void clear() {
		       $(nestedParmLoop:endNestedParmLoop4c)
		         $(primCase:endPrimCase4c)
			  factory.$(valueVar).$(varName) =
			      defaults.$(valueVar).$(varName);
			 $(endPrimCase4c)
		         $(primCaseClone:endPrimCaseClone4c)
		           try {
			       Object object =
				 (defaults.$(valueVar).$(varName) == null)?
				 null: defaults.$(valueVar).$(varName).clone();
			     if (object == null) {
			        factory.$(valueVar).$(varName) = null;
			     } else if (object instanceof $(erasedVarType)) {
			        factory.$(valueVar).$(varName) =
			          ($(varType)) object;
			     } else {
			        factory.$(valueVar).$(varName) =
			          defaults.$(valueVar).$(varName);
			     }
			   } catch(CloneNotSupportedException e) {
		                factory.$(valueVar).$(varName) =
			          defaults.$(valueVar).$(varName);
		           }
		         $(endPrimCaseClone4c)
		         $(primCaseSet:endPrimCaseSet4c)
		           factory.$(valueVar).$(varName).clear();
		         $(endPrimCaseSet4c)
                       $(endNestedParmLoop4c)
                      }
                   }));
	   $(nestedParmLoop:endNestedParmLoop4)
	      $(primCase:endPrimCase4)
	         addParm(new Parm("$(keyPrefix)$(keyDelimiter)$(parmName)",
		         $(rvClass),
	                 new ParmParser() {
		          $(parse:endParse1)
			   @SuppressWarnings("unchecked")
			   public void parse($(baseParmType) value) {
			       if ($(baseParmTest)) {
				  factory.$(valueVar).$(varName) =
				    ($(varType))value;
			       } else {
			          throw new IllegalArgumentException
				    (errorMsg("wrongType1", getParmName()));
			       }
			   }
			  $(endParse1)$(noSuppress:endNoSuppress1)
			    @SuppressWarnings("unchecked")
		            public void parse($(parmType) value) {
                               factory.$(valueVar).$(varName) =
			         $(startCall)value$(endCall);
		            }$(endNoSuppress1)
		            public void clear() {
		                factory.$(valueVar).$(varName) =
				  defaults.$(valueVar).$(varName);
		            }
		         },
		         $(type).class,
		         $(glbTerm), $(glbClosed),
		         $(lubTerm), $(lubClosed)));
              $(endPrimCase4)
	      $(primCaseClone:endPrimCaseClone4)
	         // primitive cloned case
	         addParm(new Parm("$(keyPrefix)$(keyDelimiter)$(parmName)",
		         $(rvClass),
	                 new ParmParser() {
			    @SuppressWarnings("unchecked")
		            public void parse($(parmType) value) {
		               if (value instanceof $(erasedVarType))
		                 factory.$(valueVar).$(varName) = value;
			       else
			         throw new IllegalArgumentException
				     (errorMsg("wrongType1", getParmName()));
		            }
			    @SuppressWarnings("unchecked")
		            public void clear() {
			       try {
				  Object object =
				    (defaults.$(valueVar).$(varName) == null)?
				    null:
				    defaults.$(valueVar).$(varName).clone();
				  if (object == null) {
				      factory.$(valueVar).$(varName) = null;
				  } else if(object instanceof $(erasedVarType)){
				      factory.$(valueVar).$(varName) =
				        ($(varType)) object;
				  } else {
				      factory.$(valueVar).$(varName) =
				        defaults.$(valueVar).$(varName);
				  }
			       } catch(CloneNotSupportedException e) {
		                  factory.$(valueVar).$(varName) =
				    defaults.$(valueVar).$(varName);
			       }
		            }
		         },
		         $(type).class,
		         $(glbTerm), $(glbClosed),
		         $(lubTerm), $(lubClosed)));
              $(endPrimCaseClone4)
	      $(primCaseSet:endPrimCaseSet4)
	         addParm(new Parm("$(keyPrefix)$(keyDelimiter)$(parmName)",
		         $(setofType).class, null,
	                 new ParmParser() {
		          $(parse:endParse2)
			   @SuppressWarnings("unchecked")
			   public void parse($(baseParmType) value) {
			       if ($(baseParmTest)) {
				 factory.$(valueVar).$(varName)
				     .add($(baseParmConv)(value));
			       } else {
				  throw new IllegalArgumentException
				      (errorMsg("wrongType1", getParmName()));
			       }
			   }
			  $(endParse2)$(noSuppress:endNoSuppress2)
			   @SuppressWarnings("unchecked")
		           public void parse($(parmType) value) {
			     factory.$(valueVar).$(varName).add
			         ($(startCall)value$(endCall));
			   }$(endNoSuppress2)
			   public void clear() {
			     factory.$(valueVar).$(varName).clear();
			   }
			   $(parse:endParse3)
			   public void clear($(baseParmType) value) {
			     if ($(baseParmTest)) {
			       factory.$(valueVar).$(varName).remove(value);
			     }
			   }
			   $(endParse3)$(noSuppress:endNoSuppress3)
			   public void clear($(parmType) value) {
			     factory.$(valueVar).$(varName)
				  .remove($(startCall)value$(endCall));
			   }$(endNoSuppress3)
		         },
		         null,
		         null, true, null, true));
	      $(endPrimCaseSet4)
	   $(endNestedParmLoop4)
	 $(endCompoundCase4)
	 $(keyedCase:endKeyedCase4)
	   $(hasTipCPT:endHasTipKeyed)
	       addTipResourceBundle("$(keyPrefix)", "$(keyDelimiter)",
				    "$(tipBaseName)", $(mapValueType).class);
	   $(endHasTipKeyed)
	   $(hasDocCPT:endHasDocKeyed)
	       addDocResourceBundle("$(keyPrefix)", "$(keyDelimiter)",
				    "$(docBaseName)", $(mapValueType).class);
	   $(endHasDocKeyed)
	   $(hasLabelCPT:endHasLabelKeyed)
	       addLabelResourceBundle("$(keyPrefix)", "$(keyDelimiter)",
				      "$(labelBaseName)",
				      $(mapValueType).class);
	   $(endHasLabelKeyed)
	       addParm(new Parm("$(keyPrefix)",
		       $(mapKeyType).class, null,
		       new ParmParser(factory,"$(keyPrefix)", $(mapKeyType).class) {
		       $(keyedParse:endKeyedParse1)
			@SuppressWarnings("unchecked")
		        public void parse($(mapKeyBaseParmType) key) {
			  if ($(mapKeyTest)) {
			    $(mapKeyType) mapKey = ($(mapKeyType)) key;
			    $(mapValueType) mapValue =
				factory.$(mapVar).get(mapKey);
			    if (mapValue == null) {
				mapValue = new $(mapValueType)();
				keyedTightener.$(mapVar)(mapValue);
				factory.$(mapVar).put(mapKey,mapValue);
			    }
			  } else {
			    String k = keyString(key);
			    String n = getParmName();
			    throw new IllegalArgumentException
			      (errorMsg("wrongKeyType", k, n));
			  }
			  ParmParser altParmParser = getAltParmParser();
			  if (altParmParser != null) {
			    altParmParser.parse(key);
			  }
			}
		       $(endKeyedParse1)$(keyedNoSuppress:endKeyedNoSuppress1)
		        @SuppressWarnings("unchecked")
			public void parse($(mapKeyParmType) key) {
			  $(mapKeyType) mapKey = $(mapKey);
			  $(mapValueType) mapValue =
			    factory.$(mapVar).get(mapKey);
			  if (mapValue == null) {
			    mapValue = new $(mapValueType)();
			    factory.$(mapVar).put(mapKey, mapValue);
			    keyedTightener.$(mapVar)(mapValue);
			  }
			  ParmParser altParmParser = getAltParmParser();
			  if (altParmParser != null) {
			    altParmParser.parse(key);
			  }
			}$(endKeyedNoSuppress1)
			public void clear() {
			  factory.$(mapVar).clear();
			  ParmParser altParmParser = getAltParmParser();
			  if (altParmParser != null) {
			    altParmParser.clear();
			  }
			}
		       $(keyedParse:endKeyedParse2)
			public void clear($(mapKeyBaseParmType) key) {
			  if ($(mapKeyTest)) {
			    $(mapKeyType) mapKey = ($(mapKeyType)) key;
			    factory.$(mapVar).remove(mapKey);
			  }
			  ParmParser altParmParser = getAltParmParser();
			  if (altParmParser != null) {
			    altParmParser.clear(key);
			  }
			}
		       $(endKeyedParse2)$(keyedNoSuppress:endKeyedNoSuppress2)
			public void clear($(mapKeyParmType) key) {
			  $(mapKeyType) mapKey = $(mapKey);
			  factory.$(mapVar).remove(mapKey);
			  ParmParser altParmParser = getAltParmParser();
			  if (altParmParser != null) {
			    altParmParser.clear(key);
			  }
			}$(endKeyedNoSuppress2)
		       },
		       null));
	  $(nestedParmLoop:endNestedParmLoop4)
	    $(primCase:endPrimCase4)
	       addParm(new Parm("$(keyPrefix)$(keyDelimiter)$(parmName)",
		       $(mapKeyType).class, $(rvClass),
	               new ParmParser() {
			$(parse:endParse4)
			  @SuppressWarnings("unchecked")
			  public void parse($(mapKeyBaseParmType) key,
					    $(baseParmType) value)
			  {
			    if ($(mapKeyTest)) {
			      $(mapKeyType) mapKey = ($(mapKeyType)) key;
			      if ($(baseParmTest)) {
			        $(varType) val = ($(varType)) value;
				try {factory.add("$(keyPrefix)", mapKey);}
				catch (Exception e) {}
				$(mapValueType) mapValue =
				  factory.$(mapVar).get(mapKey);
				if (mapValue == null) {
				  mapValue = new $(mapValueType)();
				  factory.$(mapVar).put(mapKey, mapValue);
				  keyedTightener.$(mapVar)(mapValue);
				}
				mapValue.$(varName) = val;
			      }
			    }
			  }
			$(endParse4)$(noSuppress:endNoSuppress4)
			  @SuppressWarnings("unchecked")
		          public void parse($(mapKeyParmType) key,
					    $(parmType) value) {
			    $(mapKeyType) mapKey = $(mapKey);
			    try {factory.add("$(keyPrefix)", mapKey);}
			    catch (Exception e) {}
			    $(mapValueType) mapValue =
				    factory.$(mapVar).get(mapKey);
			    if (mapValue == null) {
				    mapValue = new $(mapValueType)();
				    factory.$(mapVar).put(mapKey, mapValue);
				    keyedTightener.$(mapVar)(mapValue);
			    }
		            mapValue.$(varName) = $(startCall)value$(endCall);
		          }$(endNoSuppress4)
			$(parse:endParse5)
			  public void clear($(mapKeyBaseParmType) key) {
			    if ($(mapKeyTest)) {
			        $(mapKeyType) mapKey = ($(mapKeyType)) key;
				$(mapValueType) mapValue =
				    factory.$(mapVar).get(mapKey);
				if (mapValue != null) {
				    mapValue.$(varName) =
				    defaults.$(mapVar).$(varName);
				}
			    }
			  }
			$(endParse5)$(noSuppress:endNoSuppress5)
		          public void clear($(mapKeyParmType) key) {
				$(mapKeyType) mapKey = $(mapKey);
				$(mapValueType) mapValue =
				    factory.$(mapVar).get(mapKey);
				if (mapValue != null) {
				    mapValue.$(varName) =
				    defaults.$(mapVar).$(varName);
				}
		          }$(endNoSuppress5)
		       },
		       $(type).class,
		       $(glbTerm), $(glbClosed),
		       $(lubTerm), $(lubClosed)));
            $(endPrimCase4)
	    $(primCaseClone:endPrimCaseClone4)
	       addParm(new Parm("$(keyPrefix)$(keyDelimiter)$(parmName)",
		       $(mapKeyType).class, $(rvClass),
	               new ParmParser() {
		        $(parse:endParse5)
			  @SuppressWarnings("unchecked")
			  public void parse($(mapKeyBaseParmType) key,
					    $(parmType) value)
			  {
			    if (key instanceof $(mapKeyType)) {
			      $(mapKeyType) mapKey = ($(mapKeyType)) key;
			      try {factory.add("$(keyPrefix)", mapKey);}
			      catch (Exception e) {}
			      $(mapValueType) mapValue =
				    factory.$(mapVar).get(mapKey);
			      if (mapValue == null) {
				    mapValue = new $(mapValueType)();
				    keyedTightener.$(mapVar)(mapValue);
				    factory.$(mapVar).put(mapKey, mapValue);
			      }
		              if (value instanceof $(erasedVarType))
		                mapValue.$(varName) = ($(varType))value;
			      else {
				String n = getParmName();
				String k = keyString(key);
				throw new IllegalArgumentException
				  (errorMsg("wrongType2", n, k));
			      }
			    } else {
				String k = keyString(key);
				String n = getParmName();
				throw new IllegalArgumentException
				  (errorMsg("wrongKeyType", k, n));
			    }
			  }
			$(endParse5)$(noSuppress:endNoSuppress5)
			  @SuppressWarnings("unchecked")
		          public void parse($(mapKeyParmType) key,
					    $(parmType) value) {
			    $(mapKeyType) mapKey = $(mapKey);
			    try {factory.add("$(keyPrefix)", mapKey);}
			    catch (Exception e) {}
			    $(mapValueType) mapValue =
				    factory.$(mapVar).get(mapKey);
			    if (mapValue == null) {
				    mapValue = new $(mapValueType)();
				    keyedTightener.$(mapVar)(mapValue);
				    factory.$(mapVar).put(mapKey, mapValue);
			    }
		            mapValue.$(varName) = $(startCall)value$(endCall);
		          }$(endNoSuppress5)
		        $(parse:endParse6)
			  @SuppressWarnings("unchecked")
			  public void clear($(mapKeyBaseParmType) key) {
			    if (key instanceof $(mapKeyType)) {
				  $(mapKeyType) mapKey = ($(mapKeyType))key;
				  $(mapValueType) mapValue =
				    factory.$(mapVar).get(mapKey);
			      if (mapValue != null) {
				  try {
				    Object object =
					(defaults.$(mapVar).$(varName) == null)?
					  null:
					  defaults.$(mapVar).$(varName).clone();
				    if (object == null) {
				      mapValue.$(varName) = null;
				    } else if 
				      (object instanceof $(erasedVarType)) {
					 mapValue.$(varName) =
					    ($(varType)) object;
				    } else {
					 mapValue.$(varName) =
					   defaults.$(mapVar).$(varName);
				    }
				  } catch(CloneNotSupportedException e) {
					mapValue.$(varName) =
					    defaults.$(mapVar).$(varName);
				  }
			      }
			    }
		          }
			$(endParse6)$(noSuppress:endNoSuppress6)
			  @SuppressWarnings("unchecked")
		          public void clear($(mapKeyParmType) key) {
			    $(mapKeyType) mapKey = $(mapKey);
			    $(mapValueType) mapValue =
				factory.$(mapVar).get(mapKey);
			    if (mapValue != null) {
				try {
				    Object object =
					(defaults.$(mapVar).$(varName) == null)?
					null:
					defaults.$(mapVar).$(varName).clone();
				    if (object == null) {
				         mapValue.$(varName) = null;
				    } else if
				      (object instanceof $(erasedVarType)) {
					 mapValue.$(varName) =
					    ($(varType)) object;
				    } else {
					 mapValue.$(varName) =
					   defaults.$(mapVar).$(varName);
				    }
				} catch(CloneNotSupportedException e) {
				    mapValue.$(varName) =
				      defaults.$(mapVar).$(varName);
				}
			     }
		          }$(endNoSuppress6)
		       },
		       $(type).class,
		       $(glbTerm), $(glbClosed),
		       $(lubTerm), $(lubClosed)));
            $(endPrimCaseClone4)
	    $(primCaseSet:endPrimCaseSet4)
	       carray = new Class<?>[2];
	       carray[0] = $(erasedMapKeyType).class;
	       carray[1] = $(erasedMapValueVarType).class;
	       qname = new ParmKeyType(carray,  true);
	       addParm(new Parm("$(keyPrefix)$(keyDelimiter)$(parmName)",
		       qname, null,
	               new ParmParser() {
			 @SuppressWarnings("unchecked")
		         public void parse(Object[] qvalue) {
			   if (qvalue.length != 2) {
			     throw new IllegalArgumentException
				(errorMsg("not2subkeys", getParmName()));
			   }
			   if ($(qvKeyTest)
			       && qvalue[1] instanceof $(erasedMapValueVarType))
			   {
			     $(mapKeyType) mapKey =
			     ($(mapKeyType))($(rawMapKeyType)) qvalue[0];
			     $(setofType) value =
				($(setofType)) qvalue[1];
			     try {factory.add("$(keyPrefix)", mapKey);}
			     catch (Exception e) {}
			     $(mapValueType) mapValue =
				    factory.$(mapVar).get(mapKey);
			     if (mapValue == null) {
				    mapValue = new $(mapValueType)();
				    factory.$(mapVar).put(mapKey, mapValue);
			     }
			     mapValue.$(varName).add(value);
			   }
			 }
			 @SuppressWarnings("unchecked")
		         public void parse(String qvalue) {
			   String[] parms = qvalue.split("\\.");
			   $(mapKeyType) mapKey = $(qmapKey);
			   $(parmType) value = $(qvalueValueParm);
			    try {factory.add("$(keyPrefix)", mapKey);}
			    catch (Exception e) {}
			   $(mapValueType) mapValue =
				    factory.$(mapVar).get(mapKey);
			    if (mapValue == null) {
				    mapValue = new $(mapValueType)();
				    factory.$(mapVar).put(mapKey, mapValue);
			    }
			   mapValue.$(varName).add($(startCall)value$(endCall));
			 }
			 public void clear(Object[] qvalue) {
			  switch(qvalue.length) {
			  case 1:
			    if ($(qvKeyTest)) {
			      $(mapKeyType) mapKey =
				    ($(mapKeyType)) qvalue[0];
			      $(mapValueType) mapValue =
				    factory.$(mapVar).get(mapKey);
			      if (mapValue != null) {
			        mapValue.$(varName).clear();
				factory.$(mapVar).remove(mapKey);
			      }
			    } else {
			      String k = keyString(qvalue);
			      String n = getParmName();
			      throw new IllegalArgumentException
				(errorMsg("wrongKeyType", k, n));
			    }
			    break;
			  case 2:
			    if ($(qvKeyTest)
			       && qvalue[1] instanceof $(erasedMapValueVarType))
			    {
			      $(mapKeyType) mapKey =
			         ($(mapKeyType))($(rawMapKeyType)) qvalue[0];
			      $(setofType) value =
				($(setofType)) qvalue[1];
			      $(mapValueType) mapValue =
				    factory.$(mapVar).get(mapKey);
			      if (mapValue != null) {
			        mapValue.$(varName).remove(value);
			      }
			    } else {
			      String k = keyString(qvalue);
			      String n = getParmName();
			      throw new IllegalArgumentException
				(errorMsg("wrongSubkeyType", k, n));
			    }
			    break;
			  default:
			    throw new IllegalArgumentException
				(errorMsg("wrongNumberOfKeys", getParmName()));
			  }
			 }
			 public void clear(String qvalue) {
			   String[] parms = qvalue.split("\\.");
			   $(mapKeyType) mapKey = $(qmapKey);
			   if (parms.length > 1) {
			      $(parmType) value = $(qvalueValueParm);
			      $(mapValueType) mapValue =
				   factory.$(mapVar).get(mapKey);
			      if (mapValue != null) {
				mapValue.$(varName)
				  .remove($(startCall)value$(endCall));
			      }
			   } else {
			      $(mapValueType) mapValue =
				   factory.$(mapVar).get(mapKey);
			      if (mapValue != null) {
				mapValue.$(varName).clear();
			      }
			   }
			 }
		       },
		       null,
		       null, true, null, true));
	    $(endPrimCaseSet4)
	   $(endNestedParmLoop4)
	 $(endKeyedCase4)
	 $(keyedPrimitiveCase:endKeyedPrimitiveCase4)
	    $(primCase:endPrimCase4)
	       addParm(new Parm("$(keyPrefix)",
		       $(mapKeyType).class, $(rvClass),
	               new ParmParser() {
			$(parse:endParse4)
			  @SuppressWarnings("unchecked")
			  public void parse($(mapKeyBaseParmType) key,
					    $(baseParmType) value)
			  {
			    if ($(mapKeyTest)) {
			      $(mapKeyType) mapKey = ($(mapKeyType)) key;
			      if ($(baseParmTest)) {
			        $(varType) val = ($(varType)) value;
				factory.$(mapVar).put(mapKey, val);
			      }
			    }
			  }
			$(endParse4)$(noSuppress:endNoSuppress4)
			  @SuppressWarnings("unchecked")
		          public void parse($(mapKeyParmType) key,
					    $(parmType) value) {
			    $(mapKeyType) mapKey = $(mapKey);
			    $(varType) val = $(startCall)value$(endCall);
			    factory.$(mapVar).put(mapKey, val);
		          }$(endNoSuppress4)
			$(parse:endParse5)
			  public void clear($(mapKeyBaseParmType) key) {
			    if ($(mapKeyTest)) {
			        $(mapKeyType) mapKey = ($(mapKeyType)) key;
				factory.$(mapVar).remove(mapKey);
			    }
			  }
			$(endParse5)$(noSuppress:endNoSuppress5)
		          public void clear($(mapKeyParmType) key) {
				$(mapKeyType) mapKey = $(mapKey);
				factory.$(mapVar).remove(mapKey);
		          }$(endNoSuppress5)
			public void clear() {
			  factory.$(mapVar).clear();
			}
		       },
		       $(type).class,
		       $(glbTerm), $(glbClosed),
		       $(lubTerm), $(lubClosed)));
            $(endPrimCase4)
	    $(primCaseClone:endPrimCaseClone4)
	       addParm(new Parm("$(keyPrefix)",
		       $(mapKeyType).class, $(rvClass),
	               new ParmParser() {
		        $(parse:endParse5)
			  @SuppressWarnings("unchecked")
			  public void parse($(mapKeyBaseParmType) key,
					    $(parmType) value)
			  {
			    if (key instanceof $(mapKeyType)) {
			      $(mapKeyType) mapKey = ($(mapKeyType)) key;
		              if (value instanceof $(erasedVarType))
			       factory.$(mapVar).put(mapKey, ($(varType))value);
			      else {
				String n = getParmName();
				String k = keyString(key);
				throw new IllegalArgumentException
				  (errorMsg("wrongType2", n, k));
			      }
			    } else {
				String k = keyString(key);
				String n = getParmName();
			       throw new IllegalArgumentException
				 (errorMsg("wrongKeyType", k, n));
			    }
			  }
			$(endParse5)$(noSuppress:endNoSuppress5)
			  @SuppressWarnings("unchecked")
		          public void parse($(mapKeyParmType) key,
					    $(parmType) value) {
			    $(mapKeyType) mapKey = $(mapKey);
			    $(mapValueType) mapValue =
				    factory.$(mapVar).get(mapKey);
		            $(varType) val = $(startCall)value$(endCall);
			    factory.$(mapVar).put(mapKey, val);
		          }$(endNoSuppress5)
		        $(parse:endParse6)
			  @SuppressWarnings("unchecked")
			  public void clear($(mapKeyBaseParmType) key) {
			    if (key instanceof $(mapKeyType)) {
				  $(mapKeyType) mapKey = ($(mapKeyType))key;
				  factory.$(mapVar).remove(mapKey);
			    }
		          }
			$(endParse6)$(noSuppress:endNoSuppress6)
			  @SuppressWarnings("unchecked")
		          public void clear($(mapKeyParmType) key) {
			    $(mapKeyType) mapKey = $(mapKey);
			    factory.$(mapVar).remove(mapKey);
		          }
			$(endNoSuppress6)
			public void clear() {
			  factory.$(mapVar).clear();
			}
		       },
		       $(type).class,
		       $(glbTerm), $(glbClosed),
		       $(lubTerm), $(lubClosed)));
            $(endPrimCaseClone4)
	    $(primCaseSet:endPrimCaseSet4)
	       carray = new Class<?>[2];
	       carray[0] = $(erasedMapKeyType).class;
	       carray[1] = $(erasedMapValueVarType).class;
	       qname = new ParmKeyType(carray,true);
	       addParm(new Parm("$(keyPrefix)",
		       qname, null,
	               new ParmParser() {
			 @SuppressWarnings("unchecked")
		         public void parse(Object[] qvalue) {
			   if (qvalue.length != 2) {
			     throw new IllegalArgumentException
				(errorMsg("not2subkeys", getParmName()));
			   }
			   if ($(qvKeyTest)
			       && qvalue[1] instanceof $(erasedMapValueVarType))
			   {
			     $(mapKeyType) mapKey =
			     ($(mapKeyType))($(rawMapKeyType)) qvalue[0];
			     $(setofType) value =
				($(setofType)) qvalue[1];
			     $(mapValueType) mapValue =
				    factory.$(mapVar).get(mapKey);
			     if (mapValue == null) {
			       $(isEnumSet:endIsEnumSet)
				  mapValue =
				     java.util.EnumSet.noneOf
				        ($(setofType).class);
                                $(endIsEnumSet)$(isNotEnumSet:endIsNotEnumSet)
				  mapValue = new $(mapValueType)();
                                $(endIsNotEnumSet)
				factory.$(mapVar).put(mapKey, mapValue);
			     }
			     mapValue.add(value);
			   }
			 }
			 @SuppressWarnings("unchecked")
		         public void parse(String qvalue) {
			   String[] parms = qvalue.split("\\.");
			   $(mapKeyType) mapKey = $(qmapKey);
			   $(parmType) value = $(qvalueValueParm);
			   $(mapValueType) mapValue =
				    factory.$(mapVar).get(mapKey);
			    if (mapValue == null) {
			       $(isEnumSet:endIsEnumSet)
				  mapValue =
				     java.util.EnumSet.noneOf
				        ($(setofType).class);
                                $(endIsEnumSet)$(isNotEnumSet:endIsNotEnumSet)
				  mapValue = new $(mapValueType)();
                                $(endIsNotEnumSet)
				factory.$(mapVar).put(mapKey, mapValue);
			    }
			   mapValue.add($(startCall)value$(endCall));
			 }
			 public void clear(Object[] qvalue) {
			  switch(qvalue.length) {
			  case 1:
			    if ($(qvKeyTest)) {
			      $(mapKeyType) mapKey =
				    ($(mapKeyType)) qvalue[0];
			      $(mapValueType) mapValue =
				    factory.$(mapVar).get(mapKey);
			      if (mapValue != null) {
			        mapValue.clear();
				factory.$(mapVar).remove(mapKey);
			      }
			    } else {
			      String k = keyString(qvalue);
			      String n = getParmName();
			      throw new IllegalArgumentException
				(errorMsg("wrongKeyType", k, n));
			    }
			    break;
			  case 2:
			    if ($(qvKeyTest)
			       && qvalue[1] instanceof $(erasedMapValueVarType))
			    {
			      $(mapKeyType) mapKey =
			         ($(mapKeyType))($(rawMapKeyType)) qvalue[0];
			      $(setofType) value =
				($(setofType)) qvalue[1];
			      $(mapValueType) mapValue =
				    factory.$(mapVar).get(mapKey);
			      if (mapValue != null) {
			        mapValue.remove(value);
			      }
			    } else {
			      String k = keyString(qvalue);
			      String n = getParmName();
			      throw new IllegalArgumentException
				(errorMsg("wrongSubkeyType", k, n));
			    }
			    break;
			  default:
			    throw new IllegalArgumentException
				(errorMsg("wrongNumberOfKeys", getParmName()));
			  }
			 }
			 public void clear(String qvalue) {
			   String[] parms = qvalue.split("\\.");
			   $(mapKeyType) mapKey = $(qmapKey);
			   if (parms.length > 1) {
			      $(parmType) value = $(qvalueValueParm);
			      $(mapValueType) mapValue =
				   factory.$(mapVar).get(mapKey);
			      if (mapValue != null) {
				mapValue.remove($(startCall)value$(endCall));
			      }
			   } else {
			      $(mapValueType) mapValue =
				   factory.$(mapVar).get(mapKey);
			      if (mapValue != null) {
				mapValue.clear();
			      }
			   }
			 }
			public void clear() {
			  factory.$(mapVar).clear();
			}
		       },
		       null,
		       null, true, null, true));
	    $(endPrimCaseSet4)
	 $(endKeyedPrimitiveCase4)
	$(endLoop4)
    }
}
