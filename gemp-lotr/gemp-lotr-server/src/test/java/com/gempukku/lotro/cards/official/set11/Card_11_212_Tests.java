package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_212_Tests
{

	// One forest on the path by site 2: Bag End at site 1 is none, Breeland Forest at site 2 is one.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lost", "11_212");
					put("nelya", "2_84");       // Úlairë Nelya, Ringwraith in Twilight: a [wraith] Nazgûl, strength 10; nothing below 2 burdens or 2 wounds on the Ring-bearer
					put("khamul", "103_61");    // Úlairë Attëa, Khamûl of the East: a [raider] Nazgûl, strength 12, fierce; nothing below 3 burdens
					put("creeper", "8_75");     // Morgul Creeper: a [wraith] Orc and no Nazgûl, strength 8; nothing below 3 burdens
					put("creeper2", "8_75");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7, not overwhelmed unless his strength is tripled
					put("bounder", "1_286");    // Bounder: a Hobbit ally, home site 2; a skirmish ability used only on purpose
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "1_329");      // Breeland Forest: Forest; stealth events may not be played
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Two forests on the path by site 2: Hobbiton Woods at site 1 and Breeland Forest at site 2.
	protected VirtualTableScenario GetTwoForestsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lost", "11_212");
					put("lost2", "11_212");
					// Black Breath: another [wraith] condition, which a companion can bear; its bar on removing wounds
					// and burdens does not come into these tests
					put("breath", "1_207");
					put("nelya", "2_84");
					put("khamul", "103_61");
					put("creeper", "8_75");
					put("aragorn", "1_89");
					put("boromir", "1_96");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site1", "2_116");      // Hobbiton Woods: Forest; a fellowship ability used only on purpose
					put("site2", "1_329");      // Breeland Forest
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LostintheWoodsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Lost in the Woods
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Skirmish:</b> Transfer this condition from your support area to a companion your Nazgûl is skirmishing. Limit 1 per companion.<br>Each [wraith] minion skirmishing bearer is strength +1 for each forest site you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lost");

		assertEquals("Lost in the Woods", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void GoesOnlyToACompanionSkirmishingYourNazgulAndMakesEachWraithMinionThereStrongerForOneForest() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lost = scn.GetShadowCard("lost");
		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var creeper = scn.GetShadowCard("creeper");
		var creeper2 = scn.GetShadowCard("creeper2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.MoveCardsToSupportArea(lost);
		scn.MoveMinionsToTable(nelya, khamul, creeper, creeper2);

		scn.SkipToAssignments();
		// Site 2, Bounder's home: he may be assigned.
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, creeper}, new PhysicalCardImpl[]{bounder, nelya},
				new PhysicalCardImpl[]{aragorn, khamul});
		// The second Creeper is left over, and the Shadow player puts it with Khamûl.
		scn.ShadowAssignToMinions(aragorn, creeper2);

		// First, Boromir against a [wraith] Orc who is no Nazgûl.  Bounder and Aragorn are assigned to Nazgûl, but
		// their skirmishes have not begun: nobody is skirmishing a Nazgûl.
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(lost));
		scn.ShadowPass();
		// 8 against 7: Boromir loses.
		assertEquals(1, scn.GetWoundsOn(boromir));

		// Bounder, an ally, against Nelya: a Nazgûl, but no companion.
		scn.FreepsResolveSkirmish(bounder);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(lost));
		scn.ShadowPass();
		// 10 against 2: Bounder is overwhelmed.
		assertInZone(Zone.DEAD, bounder);

		// Aragorn against Khamûl, a [raider] Nazgûl, and the second Creeper.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(8, scn.GetStrength(creeper2));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(10, scn.GetStrength(nelya));
		assertEquals(8, scn.GetStrength(creeper));
		assertTrue(scn.ShadowActionAvailable(lost));

		scn.ShadowUseCardAction(lost);

		// Aragorn is the one character there, so nothing is asked.
		assertAttachedTo(lost, aragorn);
		// One forest: the [wraith] Creeper skirmishing Aragorn is +1.  Khamûl is no [wraith] minion, and Nelya and
		// the first Creeper are skirmishing nobody.
		assertEquals(9, scn.GetStrength(creeper2));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(10, scn.GetStrength(nelya));
		assertEquals(8, scn.GetStrength(creeper));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// It is no longer in your support area and offers nothing.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(lost));
	}

	@Test
	public void OneToACompanionWhoMayBearAnotherWraithConditionAndEachWraithMinionThereIsStrongerForTwoForests() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoForestsScenario();

		var lost = scn.GetShadowCard("lost");
		var lost2 = scn.GetShadowCard("lost2");
		var breath = scn.GetShadowCard("breath");
		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var creeper = scn.GetShadowCard("creeper");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.MoveCardsToSupportArea(lost, lost2);
		scn.AttachCardsTo(aragorn, breath);
		scn.MoveMinionsToTable(nelya, khamul, creeper);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, nelya}, new PhysicalCardImpl[]{boromir, khamul});
		// The Creeper is left over, and the Shadow player puts it with Khamûl.
		scn.ShadowAssignToMinions(boromir, creeper);

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(10, scn.GetStrength(nelya));
		// Black Breath on Aragorn is no Lost in the Woods.
		assertTrue(scn.ShadowActionAvailable(lost));
		assertTrue(scn.ShadowActionAvailable(lost2));

		scn.ShadowUseCardAction(lost);
		assertAttachedTo(lost, aragorn);
		assertAttachedTo(breath, aragorn);
		// Two forests: Hobbiton Woods and Breeland Forest.
		assertEquals(12, scn.GetStrength(nelya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Limit 1 per companion: the second copy may not go to Aragorn.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(lost2));
		assertFalse(scn.ShadowActionAvailable(lost));
		assertInZone(Zone.SUPPORT, lost2);
		assertEquals(12, scn.GetStrength(nelya));
		scn.ShadowPass();

		// Boromir is another companion: the second copy goes to him.  Until it does, the [wraith] Creeper in this
		// skirmish is skirmishing no bearer of the card.
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(8, scn.GetStrength(creeper));
		assertTrue(scn.ShadowActionAvailable(lost2));
		// The first copy, borne by Aragorn, is no longer in the support area: it is not moved on to Boromir.
		assertFalse(scn.ShadowActionAvailable(lost));

		scn.ShadowUseCardAction(lost2);
		assertAttachedTo(lost2, boromir);
		// Khamûl is no [wraith] minion.
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(10, scn.GetStrength(creeper));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
