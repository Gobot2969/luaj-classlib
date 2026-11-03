package org.luaj.vm2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

public class LuaClassManager {
	
	public static final String ROOT_CLASSNAME = "base";
	public static final String CLASSNAME_VAR = "CLASSNAME";
	public static final String CLASSNAME_BASE_VAR = "CLASSNAME_BASE";
	public static final String CLASS_SCRIPT_VAR = "CLASS_SCRIPT";
	// public static final String CLASSMETA_SCRIPT_VAR = "CLASSMETA_SCRIPT";
	// public static final String CLASSMETA_VAR = "CLASSMETA";
	
	/**
	 * A map of all loaded class tables: {@code <String className, LuaTable classTable>}
	 */
	public Map<String, ClassLuaTable> classes;
	
	protected String classDir = "";
	protected String staticTableName = "";
	protected String classTypeName = "";
	
	public LuaClassManager(String classDir, String classTypeName, String staticTableName, Map<String, ClassLuaTable> classes) {
		this.classDir = classDir;
		this.classTypeName = classTypeName;
		this.staticTableName = staticTableName;
		this.classes = classes;
	}
	
	public LuaClassManager(String classDir, String classTypeName, String staticTableName) {
		this(classDir, classTypeName, staticTableName, new HashMap<String, ClassLuaTable>());
	}
	
	/**
	 * Called whenever this class manager needs a new instance of a LuaClassTable. Allows overriding too.
	 * @param className
	 * @return
	 */
	protected ClassLuaTable createClassTable(String className) { // TODO check maybe if exist?
		ClassLuaTable classLuaTable = new ClassLuaTable(this, className);
		classLuaTable.classManager = this;
		return classLuaTable;
	}

	public InstanceLuaTable createInstanceTable(ClassLuaTable classTable) {
		return new InstanceLuaTable(classTable);
	}
	
	private Globals newClassmetaGlobals() {
		LuaTable staticTable = new LuaTable();
		Globals globals =  new Globals(); //new Globals();//
		LuaTable vals = LuaValue.tableOf();
		vals.set(staticTableName + "_META", staticTable);
		vals.set("pairs", LuaValue.FALSE);
		LuaTable nopeMetatable = newNopeMetatable(vals);
		globals.setmetatable(nopeMetatable);
		org.luaj.vm2.compiler.LuaC.install(globals);
		return globals;
	}
	
	private LuaTable newNopeMetatable(LuaTable vals) {
		LuaTable mt = new LuaTable();
		mt.set(LuaValue.ADD, new TwoArgFunction() {

			@Override
			public LuaValue call(LuaValue arg1, LuaValue arg2) {
				return NIL;
			}
			
		});
		mt.set(LuaValue.SUB, new TwoArgFunction() {

			@Override
			public LuaValue call(LuaValue arg1, LuaValue arg2) {
				return NIL;
			}
			
		});
		mt.set(LuaValue.DIV, new TwoArgFunction() {

			@Override
			public LuaValue call(LuaValue arg1, LuaValue arg2) {
				return NIL;
			}
			
		});
		mt.set(LuaValue.MUL, new TwoArgFunction() {

			@Override
			public LuaValue call(LuaValue arg1, LuaValue arg2) {
				return NIL;
			}
			
		});
		mt.set(LuaValue.CONCAT, new TwoArgFunction() {

			@Override
			public LuaValue call(LuaValue arg1, LuaValue arg2) {
				return NIL;
			}
			
		});
		mt.set(LuaValue.INDEX, new TwoArgFunction() {

			@Override
			public LuaValue call(LuaValue arg1, LuaValue arg2) {
				LuaValue result = vals.get(arg2);
				if (!result.isnil()) {
					return result;
				}
				return NIL;
			}
			
		});
		mt.set(LuaValue.NEWINDEX, new ThreeArgFunction() {

			@Override
			public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
				return NIL;
			}
			
		});
		mt.set(LuaValue.CALL, new ZeroArgFunction() {

			@Override
			public LuaValue call() {
				return NIL;
			}
			
			
			
		});
		
		return mt;
		
	}
	
	// public void registerAllClasses() {
	// 	//VFSFile[] classes = GE.i.filesys.listFiles(new FilePath("lua/"+this.classDir), false, true, false, new String[]{}); // foldiers only
	// 	VFSFile[] classFiles = GE.i.filesys.listFiles(new FilePath("lua/"+this.classDir), false, true, true, new String[]{"lua"}); // root lua files too
		
	// 	Map<String, ClassLuaTable> tempDuplicateCheck = new TreeMap<>();
		
	// 	for (VFSFile vf : classFiles) {
			
	// 		String className, classScript, classmetaScript;
	// 		if (vf.isDirectory()) { // If it's a folder named classname with classmeta.lua and/or init.lua inside it
	// 			// Create the class name and script paths
	// 			className = vf.getName().toLowerCase();
	// 			classScript = this.classDir + className + "/init.lua";
	// 			classmetaScript = this.classDir + className + "/classmeta.lua";
	// 			if (!GE.i.filesys.fileExists("lua/"+classmetaScript)) {
	// 				if (!GE.i.filesys.fileExists("lua/"+classScript)) { // If neither exist?!
	// 					continue; // Quit it. No class, no class.
	// 				}
	// 				classmetaScript = classScript;
	// 			}
				
	// 		} else if (vf.getExtension().equals("lua")) { // If it's just a lua file name classname lying flat in classes dir
	// 			// Create the class name and script paths
	// 			className = vf.getName().toLowerCase();
	// 			classmetaScript = this.classDir + className;
	// 			className = className.substring(0, className.length()-4); // Remove .lua from classname
				
	// 			classScript = classmetaScript;
	// 		} else { // If it's something else (not dir or  that doesn't belong in classes dir -- shouldn't occur given VFS.listFiles call allows DIRS AND FILE EXT .LUA
	// 			continue;
	// 		}
			
			
	// 		// NEW CHECK if class already exists (i.e. '{classDir}/trigger_myentclass.lua' and '{classDir}/trigger_myent/classmeta.lua' both exist and second registry attempt is ocurring)
	// 		ClassLuaTable check = tempDuplicateCheck.get(className);
	// 		if (check != null) {
	// 			GE.i.err("ABORTING DUPLICATE ATTEMPTS TO REGISTER '" + classTypeName + "' CLASS: className='" + className + "' original_classmetaScript='" + check.getClassmetaScript() + "' ; new_classmetaScript='" + classmetaScript + "'");
	// 			continue;
	// 		}
			
			
	// 		// Create the tricky globals to run the file once with and just see the base class value, etc (REPLACES ORIGINAL classmeta.lua METHOD)
	// 		Globals classmetaGlobals = newClassmetaGlobals();
	// 		// "Meta-run"
	// 		try {
	// 			this.localShared.scriptEngine.include(LuaString.valueOf(classmetaScript), classmetaGlobals, null, true); // Run in suppress error mode
	// 		} catch (Exception e) { // SILENCER MOD UNTIL WE PERFECTED "META-EXECUTION"
	// 			//GE.i.err(e.getMessage());
	// 			//e.printStackTrace(GE.i.getConErrStream());
	// 		}
			
	// 		//Retrieve the meta
	// 		LuaTable classmeta = (LuaTable)classmetaGlobals.get(staticTableName + "_META");
	// 		LuaValue base = classmeta.get(CLASSNAME_BASE_VAR);
	// 		//GE.i.log("BAAAAAAAAAAAAAAAAAAAAAAAAAAASSEEEE BAAAASE BAAASE BAAAAAAAASE: " + className + " extends " + base.tojstring());
			
	// 		// ADDED RECENTLY. We actually create an empty "loaded" class table ahead of time because reasons.
	// 		ClassLuaTable classTable = createClassTable(className);
	// 		classTable.setBaseClassName(base.tojstring());
	// 		classTable.setClassScript(classScript);
	// 		classTable.setClassmetaScript(classmetaScript);
	// 		classTable.setClassmeta(classmeta);
	// 		classes.put(className, classTable); // Save class table to classman
	// 		tempDuplicateCheck.put(className, classTable); // Just for registering all classes duplicate check
	// 		//GE.i.log("CLASS: " + className + "\t\t\t RASS:" + classScript + "\t\t\t METASS:" + classmetaScript);
	// 	}
		
	// }
	
	public void loadAllClasses(Globals globals) {
		
		List<ChildClassDepthElement> classDepthList = new ArrayList<ChildClassDepthElement>(this.classes.size()); // Create list to sort by superclass depth (i.e. ent class 'base' has depth of 0)
		
		for (String className : this.classes.keySet()) {
			classDepthList.add(new ChildClassDepthElement(className));
		}
		Collections.sort(classDepthList); // Sort from least subclassing depth to greatest (and then by class names). This ensures that base classes load before their children
		
		// Go through sorted list and finally load class
		for (ChildClassDepthElement de : classDepthList) {
			this.loadClass(de.getClassName(), globals);
			//GE.i.log("Class load order " + de.getClassName() + " with depth " + de.getDepth() );
		}
	}
	
	/**
	 * MUST BE CALLED ON CLASS NAME AFTER ALL ITS SUPERCLASSES HAD THIS METHOD INVOKED ON THEM
	 * @param className
	 * @param globals
	 */
	public void loadClass(String className, Globals globals) {
		updateInheritance(className);
		
		ClassLuaTable classTable = this.classes.get(className);
		
		globals.set(this.staticTableName+"_META", classTable);
		globals.set(this.staticTableName, classTable);
		((LuaTable)globals.get(this.staticTableName)).set(CLASSNAME_BASE_VAR, this.classes.get(className).getBaseClassName());
		
		classTable.set("__index", classTable);
		
		globals.loadfile(classTable.getClassScript());
		globals.set(this.staticTableName, LuaValue.NIL); // Hide the static class table reference from the rest of Lua
	}
	
	public boolean isSubclass(String className, String parentClassName) {
		return isSubclass(classes.get(className.toLowerCase()), classes.get(parentClassName.toLowerCase()));
	}
	
	public boolean isSubclass(ClassLuaTable classTable, ClassLuaTable parentClassTable) {
		do {
			if (classTable.getClassName().equals(parentClassTable.getClassName())) {
				return true;
			}
			classTable = classes.get(classTable.getBaseClassName());
			
			if (classTable == null) {
				break;
			}
			
		} while (classTable.getBaseClassName() != null);
		
		return false;
	}
	
	public void updateInheritance(String className) {
		
		ClassLuaTable classTable = this.classes.get(className);
		ClassLuaTable baseClassTable;

		if (classTable.getBaseClassName().equals(className)) { // Class can't inherit itself
			return;
		}
		baseClassTable = classes.get(classTable.getBaseClassName());
		if (baseClassTable == null) {
			return;
		}
		
		// Set the class table's metatable to base class.
		//classTable.setmetatable(baseClassTable); // This is oldmethod. Now we use newmethod which just copies values. No meta but instance to class relations.
		
		for (LuaValue key : baseClassTable.keys()) {
			if (key.isstring()) {
				if (!key.tojstring().equals(CLASSNAME_BASE_VAR) && !key.tojstring().equals(CLASSNAME_VAR)) {
					classTable.set(key, baseClassTable.get(key));
				}
			}
		}
	}
	
	public ClassLuaTable getRegisteredClass(String cName) {
		return classes.get(cName);
	}
	
	public LuaTable getRegisteredClasses() {
		LuaTable classesout = LuaValue.tableOf();
		for (String cName : classes.keySet()) {
			classesout.set(cName, classes.get(cName));
		}
		return classesout;
	}
	
	private class ChildClassDepthElement implements Comparable<ChildClassDepthElement> {
		private String className;
		private ClassLuaTable classTable;
		private int depth = 0;
		public ChildClassDepthElement(String className) {
			this.className = className;
			this.classTable = classes.get(this.className);
			updateDepth(this.classTable);
		}
		
		private void updateDepth(ClassLuaTable classTable) {
			ClassLuaTable baseClassTable;
			
			baseClassTable = classes.get(classTable.getBaseClassName());
			if (baseClassTable == null) {
				return;
			}
			
			depth++;
			updateDepth(baseClassTable);
		}
		
		public String getClassName() {
			return className;
		}

		public int getDepth() {
			return depth;
		}

		@Override
		public int compareTo(ChildClassDepthElement o) {
			// Check same depths
			if (this.depth == o.depth) {
				return this.className.compareTo(o.className); // Fallback to sort by string class name
			}
			
			// Different depths compare
			return this.depth - o.depth;
		}
	}
}
