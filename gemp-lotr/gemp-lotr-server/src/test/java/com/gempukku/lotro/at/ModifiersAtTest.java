package com.gempukku.lotro.at;

import com.gempukku.lotro.cards.build.DefaultActionContext;
import com.gempukku.lotro.cards.build.LotroCardBlueprintBuilder;
import com.gempukku.lotro.cards.build.ModifierSource;
import com.gempukku.lotro.cards.build.field.effect.modifier.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.PlayConditions;
import org.json.simple.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

public class ModifiersAtTest extends AbstractAtTest {
    private LotroCardBlueprintBuilder lotroCardBlueprintBuilder = new LotroCardBlueprintBuilder();

    @Test
    public void addKeywordFromCards() throws Exception {
        initializeSimplestGame();

        PhysicalCard gimli = addToZone(createCard(P1, "5_7"), Zone.FREE_CHARACTERS);
        PhysicalCard ringBearer = getRingBearer(P1);

        passUntil(Phase.FELLOWSHIP);

        JSONObject obj = new JSONObject();
        obj.put("filter", "ringBearer");
        obj.put("from", "companion,dwarf");

        ModifierSource modifierSource = new AddKeywordFromCards().getModifierSource(obj, lotroCardBlueprintBuilder);
        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);

        assertFalse(hasKeyword(ringBearer, Keyword.DAMAGE));
        addModifier(modifierSource.getModifier(actionContext));
        assertTrue(hasKeyword(ringBearer, Keyword.DAMAGE));
    }

    @Test
    public void addSignet() throws Exception {
        initializeSimplestGame();

        PhysicalCard gimli = addToZone(createCard(P1, "5_7"), Zone.FREE_CHARACTERS);

        passUntil(Phase.FELLOWSHIP);

        JSONObject obj = new JSONObject();
        obj.put("filter", "companion,dwarf");
        obj.put("signet", "Frodo");

        ModifierSource modifierSource = new AddSignet().getModifierSource(obj, lotroCardBlueprintBuilder);
        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);

        assertFalse(hasSignet(gimli, Signet.FRODO));
        addModifier(modifierSource.getModifier(actionContext));
        assertTrue(hasSignet(gimli, Signet.FRODO));
    }

    @Test
    public void allyCanParticipateInArcheryFire() throws Exception {
        initializeSimplestGame();

        PhysicalCard theGaffer = addToZone(createCard(P1, "1_291"), Zone.SUPPORT);

        passUntil(Phase.FELLOWSHIP);

        JSONObject obj = new JSONObject();
        obj.put("filter", "ally,hobbit");

        ModifierSource modifierSource = new AllyCanParticipateInArcheryFire().getModifierSource(obj, lotroCardBlueprintBuilder);
        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);

        assertFalse(_game.getModifiersQuerying().isAllyAllowedToParticipateInArcheryFire(_game, theGaffer));
        addModifier(modifierSource.getModifier(actionContext));
        assertTrue(_game.getModifiersQuerying().isAllyAllowedToParticipateInArcheryFire(_game, theGaffer));
    }

    @Test
    public void allyCanParticipateInSkirmishes() throws Exception {
        initializeSimplestGame();

        PhysicalCard theGaffer = addToZone(createCard(P1, "1_291"), Zone.SUPPORT);

        passUntil(Phase.FELLOWSHIP);

        JSONObject obj = new JSONObject();
        obj.put("filter", "ally,hobbit");

        ModifierSource modifierSource = new AllyCanParticipateInSkirmishes().getModifierSource(obj, lotroCardBlueprintBuilder);
        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);

        assertFalse(_game.getModifiersQuerying().isAllyAllowedToParticipateInSkirmishes(_game, Side.FREE_PEOPLE, theGaffer));
        addModifier(modifierSource.getModifier(actionContext));
        assertTrue(_game.getModifiersQuerying().isAllyAllowedToParticipateInSkirmishes(_game, Side.FREE_PEOPLE, theGaffer));
    }

    @Test
    public void canPlayStackedCards() throws Exception {
        initializeSimplestGame();

        PhysicalCard sindriDwarvenLord = addToZone(createCard(P1, "9_10"), Zone.FREE_CHARACTERS);
        PhysicalCard greatestKingdomOfOurPeople = addToZone(createCard(P1, "1_16"), Zone.SUPPORT);
        PhysicalCard cleavingBlow = stackOn(createCard(P1, "1_5"), greatestKingdomOfOurPeople);
        PhysicalCard goblinRunner = addToZone(createCard(P2, "1_178"), Zone.SHADOW_CHARACTERS);

        passUntil(Phase.ASSIGNMENT);
        pass(P1);
        pass(P2);
        playerDecided(P1, sindriDwarvenLord.getCardId() + " " + goblinRunner.getCardId());
        selectCard(P1, sindriDwarvenLord);
        hasCardAction(P1, cleavingBlow);
    }

    @Test
    public void cantBeOverwhelmed() throws Exception {
        initializeSimplestGame();

        PhysicalCard gimli = addToZone(createCard(P1, "5_7"), Zone.FREE_CHARACTERS);

        passUntil(Phase.FELLOWSHIP);

        JSONObject obj = new JSONObject();
        obj.put("filter", "companion,dwarf");

        ModifierSource modifierSource = new CantBeOverwhelmed().getModifierSource(obj, lotroCardBlueprintBuilder);
        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);

        assertEquals(2, _game.getModifiersQuerying().getOverwhelmMultiplier(_game, gimli));
        addModifier(modifierSource.getModifier(actionContext));
        assertEquals(10000, _game.getModifiersQuerying().getOverwhelmMultiplier(_game, gimli));
    }

    @Test
    public void cantPlayCardsOn() throws Exception {
        initializeSimplestGame();

        PhysicalCard hobbitSword = addToZone(createCard(P1, "1_299"), Zone.HAND);

        JSONObject obj = new JSONObject();
        obj.put("filter", "possession");
        obj.put("on", "ringBearer");

        ModifierSource modifierSource = new CantPlayCardsOn().getModifierSource(obj, lotroCardBlueprintBuilder);
        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);

        addModifier(modifierSource.getModifier(actionContext));

        passUntil(Phase.FELLOWSHIP);
        assertNull(getCardActionId(P1, hobbitSword));
    }

    @Test
    public void cantPlayPhaseEvents() throws Exception {
        initializeSimplestGame();

        PhysicalCard elfSong = addToZone(createCard(P1, "1_39"), Zone.HAND);
        PhysicalCard elrond = addToZone(createCard(P1, "1_40"), Zone.SUPPORT);

        JSONObject obj = new JSONObject();
        obj.put("phase", "fellowship");

        ModifierSource modifierSource = new CantPlayPhaseEvents().getModifierSource(obj, lotroCardBlueprintBuilder);
        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);

        addModifier(modifierSource.getModifier(actionContext));

        passUntil(Phase.FELLOWSHIP);
        assertNull(getCardActionId(P1, elfSong));
    }

    @Test
    public void cantPlaySite() throws Exception {
        initializeSimplestGame();

        JSONObject obj = new JSONObject();
        obj.put("player", "fp");

        ModifierSource modifierSource = new CantPlaySite().getModifierSource(obj, lotroCardBlueprintBuilder);
        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);

        passUntil(Phase.FELLOWSHIP);

        assertTrue(_game.getModifiersQuerying().canPlaySite(_game, P1));
        addModifier(modifierSource.getModifier(actionContext));
        assertFalse(_game.getModifiersQuerying().canPlaySite(_game, P1));
    }

    @Test
    public void cantRemoveThreats() throws Exception {
        initializeSimplestGame();

        PhysicalCard ringBearer = getRingBearer(P1);

        JSONObject obj = new JSONObject();
        obj.put("filter", "ringBearer");

        ModifierSource modifierSource = new CantRemoveThreats().getModifierSource(obj, lotroCardBlueprintBuilder);
        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);

        passUntil(Phase.FELLOWSHIP);

        assertTrue(_game.getModifiersQuerying().canRemoveThreat(_game, ringBearer));
        addModifier(modifierSource.getModifier(actionContext));
        assertFalse(_game.getModifiersQuerying().canRemoveThreat(_game, ringBearer));
    }

    @Test
    public void cantTakeArcheryWounds() throws Exception {
        initializeSimplestGame();

        PhysicalCard haradrimMarksman = addToZone(createCard(P2, "8_60"), Zone.SHADOW_CHARACTERS);
        PhysicalCard ringBearer = getRingBearer(P1);

        passUntil(Phase.ARCHERY);
        assertTrue(_game.getModifiersQuerying().canTakeArcheryWound(_game, ringBearer));
        addToZone(createCard(P2, "8_49"), Zone.SHADOW_CHARACTERS);
        assertFalse(_game.getModifiersQuerying().canTakeArcheryWound(_game, ringBearer));
    }

    @Test
    public void cantTakeWoundsFromLosingSkirmish() throws Exception {
        initializeSimplestGame();

        PhysicalCard ringBearer = getRingBearer(P1);

        JSONObject obj = new JSONObject();
        obj.put("filter", "ringBearer");

        ModifierSource modifierSource = new CantTakeWoundsFromLosingSkirmish().getModifierSource(obj, lotroCardBlueprintBuilder);
        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);

        passUntil(Phase.FELLOWSHIP);

        assertTrue(_game.getModifiersQuerying().canTakeWoundsFromLosingSkirmish(_game, ringBearer));
        addModifier(modifierSource.getModifier(actionContext));
        assertFalse(_game.getModifiersQuerying().canTakeWoundsFromLosingSkirmish(_game, ringBearer));
    }

    @Test
    public void cantUseSpecialAbilities() throws Exception {
        initializeSimplestGame();

        PhysicalCard elrond = addToZone(createCard(P1, "1_40"), Zone.SUPPORT);

        JSONObject obj = new JSONObject();
        obj.put("phase", "fellowship");
        obj.put("filter", "elf");

        ModifierSource modifierSource = new CantUseSpecialAbilities().getModifierSource(obj, lotroCardBlueprintBuilder);
        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);
        addModifier(modifierSource.getModifier(actionContext));

        passUntil(Phase.FELLOWSHIP);
        assertNull(getCardActionId(P1, elrond));
    }

    @Test
    public void disableGameText() throws Exception {
        initializeSimplestGame();

        PhysicalCard elrond = addToZone(createCard(P1, "1_40"), Zone.SUPPORT);

        JSONObject obj = new JSONObject();
        obj.put("filter", "elf");

        ModifierSource modifierSource = new DisableGameText().getModifierSource(obj, lotroCardBlueprintBuilder);
        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);
        addModifier(modifierSource.getModifier(actionContext));

        passUntil(Phase.FELLOWSHIP);
        assertNull(getCardActionId(P1, elrond));
    }

    @Test
    public void doesNotAddToArcheryTotal() throws Exception {
        initializeSimplestGame();

        PhysicalCard goblinMarksman = addToZone(createCard(P2, "1_176"), Zone.SHADOW_CHARACTERS);

        JSONObject obj = new JSONObject();
        obj.put("filter", "minion");

        ModifierSource modifierSource = new DoesNotAddToArcheryTotal().getModifierSource(obj, lotroCardBlueprintBuilder);
        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);

        passUntil(Phase.ARCHERY);
        assertEquals(1, getArcheryTotal(Side.SHADOW));
        addModifier(modifierSource.getModifier(actionContext));
        assertEquals(0, getArcheryTotal(Side.SHADOW));
    }

    @Test
    public void fpCultureSpot() throws Exception {
        initializeSimplestGame();

        JSONObject obj = new JSONObject();
        obj.put("amount", 1);

        ModifierSource modifierSource = new FPCultureCount().getModifierSource(obj, lotroCardBlueprintBuilder);
        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);

        passUntil(Phase.FELLOWSHIP);
        assertEquals(1, GameUtils.getSpottableFPCulturesCount(_game, P1));
        addModifier(modifierSource.getModifier(actionContext));
        assertEquals(2, GameUtils.getSpottableFPCulturesCount(_game, P1));
    }

    @Test
    public void modifyInitiativeHandSize() throws Exception {
        initializeSimplestGame();

        JSONObject obj = new JSONObject();
        obj.put("amount", -4);

        ModifierSource modifierSource = new ModifyInitiativeHandSize().getModifierSource(obj, lotroCardBlueprintBuilder);
        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);

        passUntil(Phase.FELLOWSHIP);
        assertFalse(PlayConditions.hasInitiative(_game, Side.FREE_PEOPLE));
        addModifier(modifierSource.getModifier(actionContext));
        assertTrue(PlayConditions.hasInitiative(_game, Side.FREE_PEOPLE));
    }

    @Test
    public void modifyVitality() throws Exception {
        initializeSimplestGame();

        PhysicalCard ringBearer = getRingBearer(P1);

        JSONObject obj = new JSONObject();
        obj.put("filter", "ringBearer");
        obj.put("amount", 1);

        ModifierSource modifierSource = new ModifyVitality().getModifierSource(obj, lotroCardBlueprintBuilder);
        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);

        passUntil(Phase.FELLOWSHIP);
        assertEquals(4, _game.getModifiersQuerying().getVitality(_game, ringBearer));
        addModifier(modifierSource.getModifier(actionContext));
        assertEquals(5, _game.getModifiersQuerying().getVitality(_game, ringBearer));
    }

    @Test
    public void removeAllKeywords() throws Exception {
        initializeSimplestGame();

        PhysicalCard gimli = addToZone(createCard(P1, "5_7"), Zone.FREE_CHARACTERS);

        JSONObject obj = new JSONObject();
        obj.put("filter", "dwarf,companion");

        ModifierSource modifierSource = new RemoveAllKeywords().getModifierSource(obj, lotroCardBlueprintBuilder);
        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);

        passUntil(Phase.FELLOWSHIP);
        assertTrue(hasKeyword(gimli, Keyword.DAMAGE));
        addModifier(modifierSource.getModifier(actionContext));
        assertFalse(hasKeyword(gimli, Keyword.DAMAGE));
    }

    @Test
    public void unhastyCompanionCanParticipateInSkirmishes() throws Exception {
        initializeSimplestGame();

        PhysicalCard birchseed = addToZone(createCard(P1, "5_15"), Zone.FREE_CHARACTERS);

        JSONObject obj = new JSONObject();
        obj.put("filter", "ent");

        ModifierSource modifierSource = new UnhastyCompanionCanParticipateInSkirmishes().getModifierSource(obj, lotroCardBlueprintBuilder);
        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);

        passUntil(Phase.FELLOWSHIP);
        assertFalse(Filters.assignableToSkirmish(Side.FREE_PEOPLE, false, false, false).accepts(_game, birchseed));
        addModifier(modifierSource.getModifier(actionContext));
        assertTrue(Filters.assignableToSkirmish(Side.FREE_PEOPLE, false, false, false).accepts(_game, birchseed));
    }

    @Test
    public void modifyResistance() throws Exception {
        initializeSimplestGame();

        PhysicalCard ringBearer = getRingBearer(P1);
        PhysicalCard stingWeaponOfHeritage = attachTo(createCard(P1, "11_173"), ringBearer);
        PhysicalCard gimli = addToZone(createCard(P1, "5_7"), Zone.FREE_CHARACTERS);
        PhysicalCard goblinRunner = addToZone(createCard(P2, "1_178"), Zone.SHADOW_CHARACTERS);

        passUntil(Phase.MANEUVER);
        assertEquals(5, getResistance(gimli));
        selectCardAction(P1, stingWeaponOfHeritage);
        assertEquals(7, getResistance(gimli));
    }
}
