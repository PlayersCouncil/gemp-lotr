package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertAttachedTo;
import static org.junit.Assert.*;

public class Card_V3_062_Tests
{

// ----------------------------------------
// WAR HOWDAH TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("howdah1", "103_62");     // War Howdah
					put("howdah2", "103_62");     // Second copy for limit test
					put("charger", "103_41");     // Bladetusk Charger - transforms into mounted Southron
					put("stalker", "103_49");     // Desert Wind Stalker - "Southrons gain ambush (1)"
					put("southron1", "4_222");    // Desert Warrior - to check ambush grant
					put("southron2", "4_222");    // Another for stacking
					put("mumak", "5_73");         // Regular Mumak mount
					put("orc", "1_271");          // Bait

					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

// ----------------------------------------
// WAR HOWDAH GAME TEXT COPYING TESTS
// ----------------------------------------

	protected VirtualTableScenario GetDuplicateScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("howdah", "103_62");      // War Howdah
					put("charger", "103_41");     // Bladetusk Charger
					put("stalker", "103_49");     // Desert Wind Stalker - Modifier: "Southrons gain ambush (1)"
					put("lord", "4_219");         // Desert Lord - Phase Action (Archery)
					put("fanatic", "10_49");        // Desert fanatic - Required After Trigger
					put("fighter", "7_131");      // Desert Fighter - Optional After Trigger
					put("traveler", "5_76");      // Southron Traveler - Response (Before)
					put("southron", "4_222");     // Desert Warrior - baseline Southron for ambush check
					put("orc", "1_271");          // Bait

					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WarHowdahStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: War Howdah
		 * Unique: false
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support area
		 * Game Text: Bearer gains <b>archer</b>.
		 * 		Maneuver: If this is in your support area, exert a mounted Southron to transfer this to that Southron.
		 * 		Choose a Man stacked on that Southron.  Until the end of the turn, that Southron gains the game text
		 * 		of that stacked Man.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("howdah1");

		assertEquals("War Howdah", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}



	@Test
	public void WarHowdahGrantsBearerArcher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var howdah = scn.GetShadowCard("howdah1");
		var southron = scn.GetShadowCard("southron1");
		var mumak = scn.GetShadowCard("mumak");
		var orc = scn.GetShadowCard("orc");
		scn.MoveMinionsToTable(southron, orc);
		scn.AttachCardsTo(southron, mumak, howdah);

		scn.StartGame();

		assertTrue(scn.HasKeyword(southron, Keyword.ARCHER));
	}

	@Test
	public void WarHowdahTransfersToMountedSouthronAndCopiesStackedManGameText() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var howdah = scn.GetShadowCard("howdah1");
		var charger = scn.GetShadowCard("charger");
		var stalker = scn.GetShadowCard("stalker");
		var southron = scn.GetShadowCard("southron1");
		var orc = scn.GetShadowCard("orc");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(howdah, charger);
		scn.StackCardsOn(charger);
		scn.MoveMinionsToTable(stalker, southron, orc);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPass();

		// Transform Charger first
		scn.ShadowUseCardAction(charger);
		scn.ShadowChooseCards(stalker);

		// Southron has no ambush before Stalker's text is active
		assertFalse(scn.HasKeyword(southron, Keyword.AMBUSH));

		assertTrue(scn.HasKeyword(charger, Keyword.MOUNTED));
		assertTrue(scn.HasKeyword(charger, Keyword.SOUTHRON));

		scn.FreepsPass();

		// Now transfer Howdah to Charger
		assertTrue(scn.ShadowActionAvailable(howdah));
		scn.ShadowUseCardAction(howdah);
		// Charger exerts and is auto-selected as only mounted Southron with limit available

		// Choose Stalker as the Man to copy
		// Stalker auto-selected as only stacked Man

		assertAttachedTo(howdah, charger);
		assertEquals(1, scn.GetWoundsOn(charger));

		assertTrue(scn.game().getModifiersQuerying().isCardType(GetScenario().game(), charger, CardType.MINION));

		// Charger now has Stalker's text: "Southrons gain ambush (1)", as well as its keywords
		assertTrue(scn.HasKeyword(charger, Keyword.TRACKER));
		// Check that other Southron gains ambush
		assertTrue(scn.HasKeyword(southron, Keyword.AMBUSH));
		assertEquals(1, scn.GetKeywordCount(southron, Keyword.AMBUSH));

		// Charger itself should also have ambush (it's a Southron)
		assertTrue(scn.HasKeyword(charger, Keyword.AMBUSH));
		//It gets 1 from its own text
		assertEquals(2, scn.GetKeywordCount(charger, Keyword.AMBUSH));
	}

	@Test
	public void WarHowdahLimitOnePerBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var howdah1 = scn.GetShadowCard("howdah1");
		var howdah2 = scn.GetShadowCard("howdah2");
		var charger = scn.GetShadowCard("charger");
		var stalker = scn.GetShadowCard("stalker");
		var orc = scn.GetShadowCard("orc");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(howdah1, howdah2, charger);
		scn.MoveMinionsToTable(orc, stalker);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Transform Charger
		scn.ShadowUseCardAction(charger);

		scn.FreepsPass();

		// Transfer first Howdah
		scn.ShadowUseCardAction(howdah1);

		assertAttachedTo(howdah1, charger);

		// Heal Charger so it can exert again
		scn.RemoveWoundsFromChar(charger, 1);

		scn.FreepsPass();

		// Second Howdah should not be able to target Charger (limit 1 per bearer)
		assertFalse(scn.ShadowActionAvailable(howdah2));
	}

	@Test
	public void WarHowdahTransfersWithoutCopyingIfNoManStacked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var howdah = scn.GetShadowCard("howdah1");
		var southron = scn.GetShadowCard("southron1");
		var southron2 = scn.GetShadowCard("southron2");
		var mumak = scn.GetShadowCard("mumak");
		var orc = scn.GetShadowCard("orc");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(howdah);
		scn.MoveMinionsToTable(southron, southron2, orc);
		scn.AttachCardsTo(southron, mumak); // Mounted Southron, but nothing stacked
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.HasKeyword(southron2, Keyword.AMBUSH));

		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(howdah));
		scn.ShadowUseCardAction(howdah);
		// southron auto-selected as only mounted Southron

		// Howdah transfers successfully
		assertAttachedTo(howdah, southron);
		assertEquals(1, scn.GetWoundsOn(southron));

		// Bearer gains archer
		assertTrue(scn.HasKeyword(southron, Keyword.ARCHER));

		// But no game text copied (nothing stacked)
		// southron2 still has no ambush
		assertFalse(scn.HasKeyword(southron2, Keyword.AMBUSH));
	}




	// Helper to set up Charger transformed with Howdah and a specific Man stacked
	private void SetupTransformedChargerWithHowdah(VirtualTableScenario scn, PhysicalCardImpl howdah, PhysicalCardImpl charger, PhysicalCardImpl stackedMan) throws DecisionResultInvalidException {
		scn.MoveCardsToSupportArea(howdah, charger);
		scn.MoveMinionsToTable(stackedMan);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Transform Charger
		scn.ShadowUseCardAction(charger);
		if(scn.ShadowDecisionAvailable("to stack")) {
			scn.ShadowChooseCards(stackedMan);
		}

		scn.FreepsPass();

		// Transfer Howdah to Charger, copying stackedMan's text
		scn.ShadowUseCardAction(howdah);
		// Charger auto-selected, stackedMan auto-selected
	}

	@Test
	public void WarHowdahCopiesModifier_StalkerAmbushGrant() throws DecisionResultInvalidException, CardNotFoundException {
		// Test: Modifier copying (Stalker's "Southrons gain ambush (1)")
		var scn = GetDuplicateScenario();

		var howdah = scn.GetShadowCard("howdah");
		var charger = scn.GetShadowCard("charger");
		var stalker = scn.GetShadowCard("stalker");
		var southron = scn.GetShadowCard("southron");
		var orc = scn.GetShadowCard("orc");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveMinionsToTable(southron, orc);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);

		// Southron has no ambush before copying
		assertFalse(scn.HasKeyword(southron, Keyword.AMBUSH));

		SetupTransformedChargerWithHowdah(scn, howdah, charger, stalker);

		// Charger now copies Stalker's text: "Southrons gain ambush (1)"
		// Southron should now have ambush
		assertTrue(scn.HasKeyword(southron, Keyword.AMBUSH));
		assertEquals(1, scn.GetKeywordCount(southron, Keyword.AMBUSH));

		// Charger itself is a Southron too
		assertTrue(scn.HasKeyword(charger, Keyword.AMBUSH));
	}

	@Test
	public void WarHowdahCopiesPhaseAction_DesertLordArchery() throws DecisionResultInvalidException, CardNotFoundException {
		// Test: Phase Action copying (Desert Lord's Archery ability)
		// "Archery: Exert Desert Lord to exert a companion (except the Ring-bearer)"
		var scn = GetDuplicateScenario();

		var howdah = scn.GetShadowCard("howdah");
		var charger = scn.GetShadowCard("charger");
		var lord = scn.GetShadowCard("lord");
		var orc = scn.GetShadowCard("orc");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveMinionsToTable(orc);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);

		SetupTransformedChargerWithHowdah(scn, howdah, charger, lord);

		// Skip to Archery phase
		scn.PassCurrentPhaseActions(); // Finish Maneuver
		scn.FreepsPass();

		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());

		int aragornWoundsBefore = scn.GetWoundsOn(aragorn);
		int chargerWoundsBefore = scn.GetWoundsOn(charger);

		// Charger should have Desert Lord's archery ability
		assertTrue(scn.ShadowActionAvailable(charger));
		scn.ShadowUseCardAction(charger);
		// Aragorn auto-selected as only valid companion target

		assertEquals(chargerWoundsBefore + 1, scn.GetWoundsOn(charger)); // Exerted
		assertEquals(aragornWoundsBefore + 1, scn.GetWoundsOn(aragorn)); // Exerted by ability

		scn.BothPass();
	}

	@Test
	public void WarHowdahCopiesRequiredAfterTrigger_SouthronFanaticAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		// Test: Required After Trigger copying (Southron Fanatic)
		// "Each time a Man is assigned to skirmish this minion, wound that Man."
		var scn = GetDuplicateScenario();

		var howdah = scn.GetShadowCard("howdah");
		var charger = scn.GetShadowCard("charger");
		var fanatic = scn.GetShadowCard("fanatic");
		var orc = scn.GetShadowCard("orc");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveMinionsToTable(orc);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);

		SetupTransformedChargerWithHowdah(scn, howdah, charger, fanatic);

		scn.PassCurrentPhaseActions(); // Finish Maneuver
		scn.PassCurrentPhaseActions(); // Archery
		scn.FreepsChooseCard(scn.GetRingBearer()); //archery wound
		scn.PassCurrentPhaseActions(); // Assignment actions

		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Freeps assigns Charger to Aragorn - should trigger Desert fanatic's required trigger
		scn.FreepsAssignToMinions(aragorn, charger);
		scn.ShadowDeclineOptionalTrigger(); //Ambush twilight
		scn.ShadowDeclineAssignments();

		assertEquals(1, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void WarHowdahCopiesOptionalAfterTrigger_DesertFighterThreat() throws DecisionResultInvalidException, CardNotFoundException {
		// Test: Optional After Trigger copying (Desert Fighter)
		// "At the start of each skirmish involving this minion, you may remove (1) to add a threat"
		var scn = GetDuplicateScenario();

		var howdah = scn.GetShadowCard("howdah");
		var charger = scn.GetShadowCard("charger");
		var fighter = scn.GetShadowCard("fighter");
		var orc = scn.GetShadowCard("orc");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveMinionsToTable(orc);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);

		SetupTransformedChargerWithHowdah(scn, howdah, charger, fighter);

		scn.PassCurrentPhaseActions(); // Finish Maneuver
		scn.PassCurrentPhaseActions(); // Archery
		scn.FreepsChooseCard(scn.GetRingBearer()); //archery wound
		scn.PassCurrentPhaseActions(); // Assignment actions
		scn.FreepsAssignToMinions(aragorn, charger);
		scn.ShadowDeclineOptionalTrigger(); //ambush twilight
		scn.ShadowDeclineAssignments();

		int twilightBefore = scn.GetTwilight();

		scn.FreepsResolveSkirmish(aragorn);

		//1 added by Charger's own printed game text
		assertEquals(1, scn.GetThreats());

		// At start of skirmish, Desert Fighter's optional trigger should fire
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(twilightBefore - 1, scn.GetTwilight());
		//1 added by the duplicated Desert Fighter
		assertEquals(2, scn.GetThreats());
	}

	@Test
	public void WarHowdahCopiesResponse_SouthronTravelerWoundPrevention() throws DecisionResultInvalidException, CardNotFoundException {
		// Test: Response (Before) copying (Southron Traveler)
		// "Response: If a Southron is about to take a wound, remove (3) to prevent that wound."
		var scn = GetDuplicateScenario();

		var howdah = scn.GetShadowCard("howdah");
		var charger = scn.GetShadowCard("charger");
		var traveler = scn.GetShadowCard("traveler");
		var southron = scn.GetShadowCard("southron");
		var orc = scn.GetShadowCard("orc");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveMinionsToTable(southron, orc);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(15); // Extra twilight for wound prevention cost

		SetupTransformedChargerWithHowdah(scn, howdah, charger, traveler);

		scn.PassCurrentPhaseActions(); // Finish Maneuver
		scn.PassCurrentPhaseActions(); // Archery
		scn.FreepsChooseCard(scn.GetRingBearer()); //archery wound
		scn.PassCurrentPhaseActions(); // Assignment actions
		scn.FreepsAssignToMinions(aragorn, southron);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.ShadowDeclineOptionalTrigger(); //Desert Warrior's trigger

		// Aragorn (8) beats Southron (6) - Southron about to take wound
		int southronWoundsBefore = scn.GetWoundsOn(southron);
		int twilightBefore = scn.GetTwilight();

		scn.PassCurrentPhaseActions();

		// Charger should have Traveler's response available
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Wound prevented, twilight removed
		assertEquals(southronWoundsBefore, scn.GetWoundsOn(southron));
		assertEquals(twilightBefore - 3, scn.GetTwilight());
	}
}
