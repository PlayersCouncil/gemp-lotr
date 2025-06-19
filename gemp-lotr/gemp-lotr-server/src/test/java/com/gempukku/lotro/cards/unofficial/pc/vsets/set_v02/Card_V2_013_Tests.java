package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_013_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "102_13");
					put("shadowfax", "4_100");
					put("crutch", "102_77");

					put("valiant1", "65_14");
					put("valiant2", "7_224");
					put("valiant3", "18_99");

					put("shotgun", "1_231");
					put("marauder", "10_76");
					put("grishnakh", "5_100");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GandalfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Gandalf, Lathspell
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Wizard
		 * Strength: 7
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Theoden
		 * Game Text: Each mounted companion gains <b>valiant</b>.
		* 	Response: If a minion's special ability is used (except during a skirmish), spot 3 valiant companions and exert Gandalf to prevent that. 
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gandalf");

		assertEquals("Gandalf", card.getBlueprint().getTitle());
		assertEquals("Lathspell", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.THEODEN, card.getBlueprint().getSignet()); 
	}

	@Test
	public void GandalfMakesMountedCompanionsValiant() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(shadowfax);

		scn.StartGame();

		assertFalse(scn.HasKeyword(gandalf, Keyword.VALIANT));
		scn.FreepsPlayCard(shadowfax);
		assertEquals(Zone.ATTACHED, shadowfax.getZone());
		assertEquals(gandalf, shadowfax.getAttachedTo());
		assertTrue(scn.HasKeyword(gandalf, Keyword.VALIANT));
	}

	@Test
	public void GandalfCannotRespondWith2ValiantCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCompanionsToTable("valiant1", "valiant2");

		var shotgun = scn.GetShadowCard("shotgun");
		scn.MoveMinionsToTable(shotgun);

		scn.StartGame();

		scn.AddBurdens(4);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable(shotgun));

		scn.ShadowUseCardAction(shotgun);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
	}

	@Test
	public void GandalfCanBlockMinionAbilityIf3ValiantCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var valiant1 = scn.GetFreepsCard("valiant1");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCompanionsToTable("valiant1", "valiant2", "valiant3");

		var shotgun = scn.GetShadowCard("shotgun");
		scn.MoveMinionsToTable(shotgun);

		scn.StartGame();

		scn.AddBurdens(4);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable(shotgun));

		assertEquals(0, scn.GetWoundsOn(valiant1));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(shotgun));
		scn.ShadowUseCardAction(shotgun);
		//Enquea's own exertion
		assertEquals(1, scn.GetWoundsOn(shotgun));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(valiant1));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		//Lathspell's retribution no longer wounds, just blocks
		assertEquals(1, scn.GetWoundsOn(shotgun));
	}

	@Test
	public void GandalfCanBlockMinionAbilityAndWoundItIf3ValiantCompanionsAndBearingNewStaff() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var crutch = scn.GetFreepsCard("crutch");
		var valiant1 = scn.GetFreepsCard("valiant1");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, crutch);
		scn.MoveCompanionsToTable("valiant1", "valiant2", "valiant3");

		var shotgun = scn.GetShadowCard("shotgun");
		scn.MoveMinionsToTable(shotgun);

		scn.StartGame();

		scn.AddBurdens(4);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable(shotgun));

		assertEquals(0, scn.GetWoundsOn(valiant1));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(shotgun));
		scn.ShadowUseCardAction(shotgun);
		//Enquea's own exertion
		assertEquals(1, scn.GetWoundsOn(shotgun));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(valiant1));
		assertEquals(1, scn.GetWoundsOn(gandalf));

		assertEquals(1, scn.GetWoundsOn(shotgun));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		//Lathspell's retribution no longer wounds, but his staff does
		assertEquals(2, scn.GetWoundsOn(shotgun));
	}

	@Test
	public void GandalfCanBlockGrishnakhIf3ValiantCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var valiant1 = scn.GetFreepsCard("valiant1");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCompanionsToTable("valiant1", "valiant2", "valiant3");

		var grishnakh = scn.GetShadowCard("grishnakh");
		scn.MoveMinionsToTable(grishnakh);
		scn.MoveMinionsToTable("marauder");

		scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowActionAvailable(grishnakh));

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(grishnakh));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(7, scn.GetShadowDeckCount());
		scn.ShadowUseCardAction(grishnakh);
		//Grishnakh's own exertions
		assertEquals(2, scn.GetWoundsOn(grishnakh));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		//Lathspell's retribution no longer wounds
		assertEquals(Zone.SHADOW_CHARACTERS, grishnakh.getZone());
		assertEquals(2, scn.GetWoundsOn(grishnakh));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(7, scn.GetShadowDeckCount());
	}

	@Test
	public void GandalfCanBlockGrishnakhAndWoundHimIf3ValiantCompanionsAndBearingNewStaff() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var crutch = scn.GetFreepsCard("crutch");
		var valiant1 = scn.GetFreepsCard("valiant1");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, crutch);
		scn.MoveCompanionsToTable("valiant1", "valiant2", "valiant3");

		var grishnakh = scn.GetShadowCard("grishnakh");
		scn.MoveMinionsToTable(grishnakh);
		scn.MoveMinionsToTable("marauder");

		scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowActionAvailable(grishnakh));

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(grishnakh));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(7, scn.GetShadowDeckCount());
		scn.ShadowUseCardAction(grishnakh);
		//Grishnakh's own exertions
		assertEquals(2, scn.GetWoundsOn(grishnakh));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		//Lathspell's retribution no longer wounds
		assertEquals(Zone.SHADOW_CHARACTERS, grishnakh.getZone());
		assertEquals(2, scn.GetWoundsOn(grishnakh));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(7, scn.GetShadowDeckCount());

		//The staff makes up for it tho
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(grishnakh);
		assertEquals(Zone.DISCARD, grishnakh.getZone());
	}

	@Test
	public void GandalfCannotBlockSkirmishAbilityEvenWith3ValiantCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var valiant1 = scn.GetFreepsCard("valiant1");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCompanionsToTable("valiant1", "valiant2", "valiant3");

		var marauder = scn.GetShadowCard("marauder");
		scn.MoveMinionsToTable(marauder);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(valiant1, marauder);
		scn.FreepsResolveSkirmish(valiant1);

		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable(marauder));

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(marauder));
		assertEquals(9, scn.GetStrength(marauder));
		scn.ShadowUseCardAction(marauder);
		//marauder's own exertion
		assertEquals(1, scn.GetWoundsOn(marauder));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(marauder));
		//Ability went through with no objection
		assertEquals(12, scn.GetStrength(marauder));
	}
}
