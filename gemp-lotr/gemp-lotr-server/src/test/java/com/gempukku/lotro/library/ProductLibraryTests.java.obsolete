//package com.gempukku.lotro.library;
//
//import com.gempukku.lotro.at.AbstractAtTest;
//import com.gempukku.lotro.builder.PacksStorageBuilder;
//import com.gempukku.lotro.packs.PacksStorage;
//import com.gempukku.lotro.packs.ProductLibrary;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.ValueSource;
//
//import static junit.framework.TestCase.assertEquals;
//import static junit.framework.TestCase.assertNotNull;



// This file is preserved solely to be used as an example for how to perform heavy data-driven unit tests.
// The classes involved have long been replaced; see 2023-06-27 commits.




//
//public class ProductLibraryTests extends AbstractAtTest {
//
//    protected static ProductLibrary _productLib = new ProductLibrary(_cardLibrary);
//    protected static PacksStorage _packStorage = PacksStorageBuilder.createPacksStorage(_cardLibrary);
//
//    @ParameterizedTest(name = "{0} in ProductLibrary matches PacksStorage.")
//    @ValueSource(strings = {
//            "(S)BL - Starter",
//            "BL - Arwen Starter",
//            "BL - Boromir Starter",
//            "(S)BoHD - Starter",
//            "BoHD - Eowyn Starter",
//            "BoHD - Legolas Starter",
//            "(S)All Decipher Choice - Booster",
//            "(S)BR - Starter",
//            "BR - Mouth Starter",
//            "BR - Saruman Starter",
//            "(S)EoF - Starter",
//            "EoF - Faramir Starter",
//            "EoF - Witch-king Starter",
//            "(S)FotR - Starter",
//            "FotR - Aragorn Starter",
//            "FotR - Gandalf Starter",
//            "(S)HU - Starter",
//            "HU - Aragorn Starter",
//            "HU - Mauhur Starter",
//            "(S)MD - Starter",
//            "MD - Frodo Starter",
//            "MD - Sam Starter",
//            "(S)MoM - Starter",
//            "MoM - Gandalf Starter",
//            "MoM - Gimli Starter",
//            "(S)Movie Choice - Booster",
//            "(S)RoS - Starter",
//            "RoS - Uruk Rampage Starter",
//            "RoS - Evil Man Starter",
//            "(S)RotEL - Starter",
//            "RotEL - Boromir Starter",
//            "RotEL - Legolas Starter",
//            "(S)RotK - Starter",
//            "RotK - Aragorn Starter",
//            "RotK - Eomer Starter",
//            "(S)SH - Starter",
//            "SH - Aragorn Starter",
//            "SH - Eowyn Starter",
//            "SH - Gandalf Starter",
//            "SH - Legolas Starter",
//            "(S)SoG - Starter",
//            "SoG - Merry Starter",
//            "SoG - Pippin Starter",
//            "(S)TS Choice - Booster",
//            "(S)TTT - Starter",
//            "TTT - Aragorn Starter",
//            "TTT - Theoden Starter",
//            "Wraith",
//            "AgesEnd",
//            "Expanded",
//            "Special-01",
//            "Special-02",
//            "Special-03",
//            "Special-04",
//            "Special-05",
//            "Special-06",
//            "Special-07",
//            "Special-08",
//            "Special-09",
//            //"TSSealedS1D1",
//            //"TSSealedS1D2",
//            //"TSSealedS1D3",
//            "TSSealedS2D1",
//            "TSSealedS2D2",
//            "TSSealedS2D3",
//            "TSSealedS3D1",
//            "TSSealedS3D2",
//            "TSSealedS3D3",
//            "(S)Special-1-3",
//            "(S)Special-4-6",
//            "(S)Special-7-9",
//            "(S)TSSealed-S1",
//            "(S)TSSealed-S2",
//            "(S)TSSealed-S3",
//
//            "(S)FotR - Tengwar",
//            "(S)TTT - Tengwar",
//            "(S)RotK - Tengwar",
//            "(S)SH - Tengwar",
//            "(S)Tengwar",
//    })
//    public void PacksStorageComparison(String packName) {
//        var oldPack = _packStorage.GetBox(packName);
//        var newPack = _productLib.GetProduct(packName);
//
//        assertNotNull(oldPack);
//        assertNotNull(newPack);
//
//        var oldList = oldPack.openPack();
//        var newList = newPack.openPack();
//
//        assertEquals(oldList.size(), newList.size());
//
//        for(int i = 0; i < oldList.size(); ++i) {
//            var oldItem = oldList.get(i);
//            var newItem = newList.get(i);
//
//            assertEquals(oldItem, newItem);
//        }
//    }
//
//    @ParameterizedTest(name = "{0} randomized pack in ProductLibrary matches PacksStorage.")
//    @ValueSource(strings = {
//            "FotR - League Starter",
//            "Random FotR Foil Common",
//            "Random FotR Foil Uncommon",
//    })
//    public void RandomizedPacksStorageComparison(String packName) {
//        var oldPack = _packStorage.GetBox(packName);
//        var newPack = _productLib.GetProduct(packName);
//
//        assertNotNull(oldPack);
//        assertNotNull(newPack);
//
//        var oldList = oldPack.openPack(0);
//        var newList = newPack.openPack(0);
//
//        assertEquals(oldList.size(), newList.size());
//
//        for(int i = 0; i < oldList.size(); ++i) {
//            var oldItem = oldList.get(i);
//            var newItem = newList.get(i);
//
//            if(newItem.getBlueprintId().toLowerCase().contains("random")) {
//                newItem = _productLib.GetProduct(newItem.getBlueprintId()).openPack(0).stream().findFirst().get();
//            }
//
//            assertEquals(oldItem, newItem);
//        }
//    }
//
//    @ParameterizedTest(name = "{0} booster in ProductLibrary matches PacksStorage.")
//    @ValueSource(strings = {
//            "FotR - Booster",
//            "MoM - Booster",
//            "RotEL - Booster",
//
//            "TTT - Booster",
//            "BoHD - Booster",
//            "EoF - Booster",
//
//            "RotK - Booster",
//            "SoG - Booster",
//            "MD - Booster",
//
//            "SH - Booster",
//            "BR - Booster",
//            "BL - Booster",
//
//            "HU - Booster",
//            "RoS - Booster",
//            "TaD - Booster",
//
//            "REF - Booster",
//    })
//    public void BoosterPacksStorageComparison(String packName) {
//        var oldPack = _packStorage.GetBox(packName);
//        var newPack = _productLib.GetProduct(packName);
//
//        assertNotNull(oldPack);
//        assertNotNull(newPack);
//
//        var oldList = oldPack.GetAllOptions();
//        var newList = newPack.GetAllOptions();
//
//        assertEquals(oldList.size(), newList.size());
//
//        for(int i = 0; i < oldList.size(); ++i) {
//            var oldItem = oldList.get(i);
//            var newItem = newList.get(i);
//
//            assertEquals(oldItem, newItem);
//        }
//    }
//
//
//
//
//}
