package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_178_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dark", "13_178");
					put("otsea", "12_179");      // Úlairë Otsëa, Black Specter: [wraith] Nazgûl, vitality 3; a Shadow ability used only on purpose
					put("khamul", "103_61");     // Úlairë Attëa, Khamûl of the East: [raider] Nazgûl, vitality 3, fierce; the rest needs 3 burdens
					put("predator", "7_196");    // Morgul Predator: [wraith] Orc and no Nazgûl, vitality 3; a skirmish ability used only on purpose
					put("breath", "1_207");      // Black Breath: [wraith] condition; wounds may not be removed from its bearer

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: vitality 4; a maneuver ability only
					put("boromir", "1_97");      // Boromir, Son of Denethor: vitality 3; a skirmish ability only
					put("sam", "1_311");         // Sam, Son of Hamfast: vitality 4; a fellowship ability and a response when Frodo dies
					put("elrond", "1_40");       // Elrond, Lord of Rivendell: an ally, vitality 4; heals allies at the start of his owner's turns
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DarkFellAboutHimStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Dark Fell About Him
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: To play, exert your Nazgûl twice and spot an exhausted companion.<br>Heal that companion to add a burden. If the fellowship is not in region 1, you may repeat this for each other wound on that companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dark");

		assertEquals("Dark Fell About Him", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsYourNazgulTwiceAndHealsTheExhaustedCompanionSpottedToAddABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(elrond);
		var frodo = scn.GetRingBearer();

		var dark = scn.GetShadowCard("dark");
		var otsea = scn.GetShadowCard("otsea");
		var khamul = scn.GetShadowCard("khamul");
		var predator = scn.GetShadowCard("predator");

		scn.StartGame();
		scn.SkipToSite(2);
		// Frodo and Aragorn exhausted (vitality 4); Sam wounded and not exhausted; Elrond an exhausted ally.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(sam, 2);
		scn.AddWoundsToChar(elrond, 3);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(3, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetBurdens());
		// The minions are laid now and live through the turn to its regroup phase.
		scn.MoveMinionsToTable(otsea, khamul, predator);

		// The regroup phase at site 3, in region 1.
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.MoveCardsToHand(dark);
		scn.SetTwilight(3);
		assertInHand(dark);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(dark));
		scn.ShadowPlayCard(dark);

		// Either Nazgûl may exert, the [raider] one as well; not the [wraith] Orc, who could exert twice too.
		assertTrue(scn.ShadowHasCardChoiceAvailable(otsea, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(predator));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(otsea);
		assertEquals(2, scn.GetWoundsOn(otsea));
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(predator));

		// Either exhausted companion; not Sam (wounded, not exhausted), the exhausted ally, nor the Nazgûl just exhausted.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sam, elrond, otsea));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);

		// That companion is healed once and a burden added; in region 1, nothing more is asked.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.ShadowDecisionAvailable("heal"));
		assertInDiscard(dark);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotPlayableWhileYourNazgulCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var dark = scn.GetShadowCard("dark");
		var otsea = scn.GetShadowCard("otsea");
		var predator = scn.GetShadowCard("predator");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.MoveMinionsToTable(otsea, predator);
		// Vitality 3 with one wound: one exertion left.
		scn.AddWoundsToChar(otsea, 1);
		assertEquals(1, scn.GetWoundsOn(otsea));
		assertEquals(0, scn.GetWoundsOn(predator));

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToHand(dark);
		scn.SetTwilight(3);
		assertInHand(dark);
		scn.FreepsPass();

		// An exhausted companion, the pool, and a [wraith] Orc who could exert twice: but no Nazgûl who can.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(dark));
	}

	@Test
	public void NotPlayableWithoutAnExhaustedCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);
		var frodo = scn.GetRingBearer();

		var dark = scn.GetShadowCard("dark");
		var otsea = scn.GetShadowCard("otsea");

		scn.StartGame();
		scn.SkipToSite(2);
		// Two wounds each on vitality 4: wounded, with 2 vitality left, and not exhausted.
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(sam, 2);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.MoveMinionsToTable(otsea);

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToHand(dark);
		scn.SetTwilight(3);
		assertInHand(dark);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(dark));
	}

	@Test
	public void NotPlayableWhenTheOnlyExhaustedCompanionCannotBeHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var dark = scn.GetShadowCard("dark");
		var otsea = scn.GetShadowCard("otsea");
		var breath = scn.GetShadowCard("breath");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.AddWoundsToChar(aragorn, 3);
		// Black Breath, transferred to him in some skirmish: "wounds may not be removed from bearer".
		scn.AttachCardsTo(aragorn, breath);
		assertAttachedTo(breath, aragorn);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.MoveMinionsToTable(otsea);

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToHand(dark);
		scn.SetTwilight(3);
		assertInHand(dark);
		scn.FreepsPass();

		// The heal is a cost of the play (CR 4.2 "playing a card"), and he is the only exhausted companion.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(dark));
	}

	@Test
	public void OutsideRegion1TheFirstHealIsNotAskedAndIsRepeatedForEachOtherWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		var frodo = scn.GetRingBearer();

		var dark = scn.GetShadowCard("dark");
		var otsea = scn.GetShadowCard("otsea");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(frodo, 2);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		scn.MoveMinionsToTable(otsea);

		// The regroup phase at site 4, in region 2.
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.MoveCardsToHand(dark);
		scn.SetTwilight(3);
		assertInHand(dark);
		scn.FreepsPass();

		// The lone Nazgûl exerts and the lone exhausted companion is spotted, unasked; the first heal and burden are no "may".
		scn.ShadowPlayCard(dark);
		assertEquals(2, scn.GetWoundsOn(otsea));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetBurdens());

		// Two other wounds on Aragorn: it may be done twice more; Frodo's wounds are not counted.
		assertTrue(scn.ShadowDecisionAvailable("heal"));
		scn.ShadowChooseYes();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.ShadowDecisionAvailable("heal"));
		scn.ShadowChooseYes();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertFalse(scn.ShadowDecisionAvailable("heal"));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OutsideRegion1WithOneOtherWoundTheRepeatIsAskedOnceAndMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		var dark = scn.GetShadowCard("dark");
		var otsea = scn.GetShadowCard("otsea");

		scn.StartGame();
		scn.SkipToSite(3);
		// Vitality 3: exhausted with two wounds.
		scn.AddWoundsToChar(boromir, 2);
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetBurdens());
		scn.MoveMinionsToTable(otsea);

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.MoveCardsToHand(dark);
		scn.SetTwilight(3);
		assertInHand(dark);
		scn.FreepsPass();

		scn.ShadowPlayCard(dark);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetBurdens());

		// One other wound: one more is offered, and declining it leaves the rest as it was.
		assertTrue(scn.ShadowDecisionAvailable("heal"));
		scn.ShadowChooseNo();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.ShadowDecisionAvailable("heal"));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
