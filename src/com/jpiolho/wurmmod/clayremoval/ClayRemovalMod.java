/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpiolho.wurmmod.clayremoval;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.items.AdvancedCreationEntry;
import com.wurmonline.server.items.CreationCategories;
import com.wurmonline.server.items.CreationEntryCreator;
import com.wurmonline.server.items.CreationRequirement;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.skills.SkillList;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.ItemMaterials;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.Descriptor;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.Configurable;
import org.gotti.wurmunlimited.modloader.interfaces.ItemTemplatesCreatedListener;
import org.gotti.wurmunlimited.modloader.interfaces.PreInitable;
import org.gotti.wurmunlimited.modloader.interfaces.ServerStartedListener;
import org.gotti.wurmunlimited.modloader.interfaces.WurmMod;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

/**
 *
 * @author JPiolho
 */
public class ClayRemovalMod implements WurmMod,PreInitable,ItemTemplatesCreatedListener,ServerStartedListener,Configurable {
    private static Logger logger = Logger.getLogger(ClayRemovalMod.class.getName());

    
    public static int iidDecayBed;
    
    @Override
    public void preInit() {
        try {
            
            ClassPool cpool = HookManager.getInstance().getClassPool();
            
            String descriptor = Descriptor.ofMethod(CtClass.booleanType, new CtClass[] {});
            
            
            CtClass cClass = cpool.get("com.wurmonline.server.items.Item"); 
            CtMethod method = cClass.getMethod("checkDecay", descriptor);
            
            method.instrument(new ExprEditor() {

                int i = 0;
                
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if(m.getClassName().equals("com.wurmonline.server.items.Item") && m.getMethodName().equals("sendDecayMess"))
                    {
                        if(i == 0) {
                            method.insertAt(m.getLineNumber(), "{" +
                                                                "com.jpiolho.wurmmod.clayremoval.ClayRemovalMod.itemDecayInBed(this);" +
                                                               "}");
                        }
                       
                        i++;
                    }
                }
                
                
            });
        } 
        catch(Exception ex)
        {
            logger.log(Level.SEVERE, "hookEvents", ex);
        }
        
        try {
            
            ClassPool cpool = HookManager.getInstance().getClassPool();
            
            String descriptor = Descriptor.ofMethod(cpool.get("java.lang.String"), new CtClass[] {});
            
            
            CtClass cClass = cpool.get("com.wurmonline.server.items.Item"); 
            CtMethod method = cClass.getMethod("getModelName", descriptor);
            
            method.instrument(new ExprEditor() {

                int i = 0;
                
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if(m.getClassName().equals("com.wurmonline.server.items.Item") && m.getMethodName().equals("isBulkContainer"))
                    {
                        if(i == 0) {
                            method.insertAt(m.getLineNumber(), "{" +
                                                                "if(this.getTemplateId() == com.jpiolho.wurmmod.clayremoval.ClayRemovalMod.iidDecayBed) {" +
                                                                    "if(this.isEmpty()) builder.append(\"empty.\"); else builder.append(\"full.\");" +
                                                                "}" +
                                                               "}");
                        }

                        i++;
                    }
                }
                
                
            });
        } 
        catch(Exception ex)
        {
            logger.log(Level.SEVERE, "hookEvents", ex);
        }
    }

    @Override
    public void onItemTemplatesCreated() {
        try {
            ItemTemplateBuilder builder = new ItemTemplateBuilder("jp.clayremoval.decaybed");

            builder.name("decay bed","decay beds","A wooden square container that is used to let things rot over time and be absorbed by the soil.");
            builder.modelName("model.jpmod.clayremoval.decaybed.");
            builder.descriptions("excellent", "good", "ok", "poor");
            builder.itemTypes(new short[]{
                ItemTypes.ITEM_TYPE_REPAIRABLE,
                ItemTypes.ITEM_TYPE_WOOD,
                ItemTypes.ITEM_TYPE_DESTROYABLE,
                ItemTypes.ITEM_TYPE_USE_GROUND_ONLY,
                ItemTypes.ITEM_TYPE_HOLLOW,
                ItemTypes.ITEM_TYPE_TILE_ALIGNED,
                ItemTypes.ITEM_TYPE_OUTSIDE_ONLY,
                ItemTypes.ITEM_TYPE_NOTAKE,
                ItemTypes.ITEM_TYPE_TRANSPORTABLE,
                ItemTypes.ITEM_TYPE_ONE_PER_TILE
           });

            builder.imageNumber((short)60);
            builder.behaviourType((short)1);
            builder.combatDamage(0);
            builder.decayTime(9072000L);
            builder.dimensions(400, 400, 60);
            builder.primarySkill((int)MiscConstants.NOID);
            builder.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);

            builder.difficulty(1.0f);
            builder.weightGrams(50000);
            builder.material(ItemMaterials.MATERIAL_WOOD_PINE);
            builder.isTraded(false);
            builder.value(0);
        
            iidDecayBed = builder.build().getTemplateId();
        } catch(IOException ex) {
            throw new RuntimeException(ex);
        }
        
        
    }
    
    
    public static void itemDecayInBed(Item item) {
        
        Item parent = null;
        try {
            parent = item.getParent();
        }
        catch(NoSuchItemException ex) {
            return;
        }
        
        if(parent == null || parent.getTemplateId() != iidDecayBed)
            return;
        
        if(!parent.isOnSurface())
            return;

        if(item.isWood()) {
            int tile = Server.surfaceMesh.getTile(item.getTileX(), item.getTileY());
            
            if(Terraforming.isCornerUnderWater(item.getTileX(), item.getTileY(), true))
                return;
            
            
            
            if(Tiles.decodeType(tile) == Tiles.TILE_TYPE_CLAY) {
                int chance = Server.rand.nextInt(100);
            
                Village village = Zones.getVillage(item.getTileX(), item.getTileY(), true);


                float weightpercent = item.getWeightGrams() / (float)totalWeight;
                if(weightpercent > 1.0f) weightpercent = 1.0f;

                float volumepercent = item.getVolume() / (float)totalVolume;
                if (volumepercent > 1.0f) volumepercent = 1.0f;

                int totalChance = (int)Math.ceil((village == null ? percentWeight : percentVillageWeight) * weightpercent) + (int)Math.ceil((village == null ? percentVolume : percentVillageVolume) * volumepercent);
                
                logger.log(Level.INFO,"Change: " + chance + " | Total Chance: " + totalChance);
                
                if(chance <= totalChance) {

                    if(Tiles.decodeType(tile) == Tiles.TILE_TYPE_CLAY) {
                        Server.setSurfaceTile(item.getTileX(),item.getTileY(),Tiles.decodeHeight(tile),(byte)Tiles.TILE_TYPE_DIRT, Tiles.decodeData(tile));
                        Players.getInstance().sendChangedTile(item.getTileX(), item.getTileY(), true, true);
                    }
                }
            }
        }
    }

    @Override
    public void onServerStarted() {
        if(iidDecayBed > 0) {
            AdvancedCreationEntry creationEntry = CreationEntryCreator.createAdvancedEntry(SkillList.CARPENTRY, ItemList.plank, ItemList.nailsIronLarge, iidDecayBed, false, false, 0.0f, true, true, CreationCategories.PRODUCTION);
            creationEntry.addRequirement(new CreationRequirement(1,ItemList.plank,12,true));
            creationEntry.addRequirement(new CreationRequirement(2,ItemList.nailsIronSmall,4,true));
        }
    }

    
    
    private static int percentWeight,percentVolume,percentVillageWeight,percentVillageVolume;
    private static int totalWeight,totalVolume;
    
    @Override
    public void configure(Properties prop) {
        percentWeight = Integer.parseInt(prop.getProperty("weightPercent",Integer.toString(percentWeight)));
        percentVolume = Integer.parseInt(prop.getProperty("volumePercent",Integer.toString(percentVolume)));
        percentVillageWeight = Integer.parseInt(prop.getProperty("weightPercentVillage",Integer.toString(percentVillageWeight)));
        percentVillageVolume = Integer.parseInt(prop.getProperty("volumePercentVillage",Integer.toString(percentVillageVolume)));
        totalWeight = Integer.parseInt(prop.getProperty("totalWeight",Integer.toString(totalWeight)));
        totalVolume = Integer.parseInt(prop.getProperty("totalVolume",Integer.toString(totalVolume)));
        
        logger.log(Level.INFO,"weightPercent: " + percentWeight);
        logger.log(Level.INFO,"volumePercent: " + percentVolume);
        logger.log(Level.INFO,"weightPercentVillage: " + percentVillageWeight);
        logger.log(Level.INFO,"volumePercentVillage: " + percentVillageVolume);
        logger.log(Level.INFO,"totalWeight: " + totalWeight);
        logger.log(Level.INFO,"totalVolume: " + totalVolume);
    }
}
