package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_094_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "4_94");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetShadowConditionScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hearken", "4_94");
					put("theoden", "4_292");    // Théoden, Son of Thengel: a regroup ability only
					put("gimli", "1_13");       // Gimli, Son of Glóin: a companion who is no Man; a skirmish ability only
					// Weary: [isengard] condition, cost 1, "Plays on a companion or ally. Limit 1 per
					// character."; its trigger needs an [isengard] tracker
					put("weary", "4_212");
					// Banished: [isengard] condition, cost 0, "To play, exert Saruman or an [isengard] Man.
					// Plays on a Free Peoples Man."; its trigger answers a lost skirmish
					put("banished", "4_139");
					put("grima", "4_154");      // Gríma, Wormtongue: [isengard] Man, vitality 3, for Banished to exert; a maneuver ability only
					// Blade Tip: [wraith] condition in the Shadow support area, cost 0.  "Response: If your
					// Nazgûl wins a skirmish, transfer this condition from your support area to the losing
					// character."  It wounds its bearer only at the start of a fellowship phase.
					put("bladetip", "1_209");
					// Úlairë Lemenya: Nazgûl, strength 9, vitality 2, not fierce; her text is about a
					// character who bears a [wraith] condition
					put("lemenya", "1_232");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HearkentoMeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Hearken to Me
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: 
		 * Strength: 2
		 * Vitality: 1
		 * Game Text: <b>Spell</b>. To play, spot Gandalf. Bearer must be Théoden.<br>Discard all Shadow conditions on Théoden. Shadow conditions may not be played on Théoden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Hearken to Me", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void AShadowConditionCannotBePlayedOnTheodenWhileHeBearsItAndCanBePlayedOnAnotherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowConditionScenario();

		var hearken = scn.GetFreepsCard("hearken");
		var theoden = scn.GetFreepsCard("theoden");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		var weary = scn.GetShadowCard("weary");
		var banished = scn.GetShadowCard("banished");
		var grima = scn.GetShadowCard("grima");
		scn.MoveCompanionsToTable(theoden, gimli);
		scn.MoveCardsToHand(weary, banished);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		scn.FreepsPass();

		// Théoden is the only Free Peoples Man on the table, so he is the one bearer Banished could have.
		// Without Hearken to Me it may be played.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.HAND, weary, banished);
		assertTrue(scn.ShadowPlayAvailable(banished));
		assertTrue(scn.ShadowPlayAvailable(weary));

		scn.AttachCardsTo(theoden, hearken);
		scn.ShadowRefreshDecision();
		// With it, Banished has nobody to go on and is not offered at all.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(banished));
		assertTrue(scn.ShadowPlayAvailable(weary));

		// Weary goes on any companion: the other two are offered and Théoden is not.
		scn.ShadowPlayCard(weary);
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, frodo));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(theoden));

		scn.ShadowChooseCard(gimli);
		assertAttachedTo(weary, gimli);
		assertAttachedTo(hearken, theoden);
		assertEquals(0, scn.GetWoundsOn(grima));
		assertFalse(scn.ShadowPlayAvailable(banished));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// "Discard all Shadow conditions on Théoden" is a standing discard, apart from the bar that follows it (Ketura
	// on Q-0229: "the discard will be continuous and the poor-mans-cannot-bear poorly keeps them off"; Q-0223).
	// The bar is on PLAYING a Shadow condition on him, and a transfer is not a play (Q-0053): Blade Tip reaches
	// Théoden, and is discarded as it lands.
	@Test
	public void AShadowConditionTransferredToTheodenIsDiscardedAsItLands() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowConditionScenario();

		var hearken = scn.GetFreepsCard("hearken");
		var theoden = scn.GetFreepsCard("theoden");
		var bladetip = scn.GetShadowCard("bladetip");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCompanionsToTable(theoden);
		scn.AttachCardsTo(theoden, hearken);
		scn.MoveCardsToSupportArea(bladetip);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(theoden, lemenya);
		scn.FreepsResolveSkirmish(theoden);
		// 6 + 2 for Hearken to Me, against 9
		assertEquals(8, scn.GetStrength(theoden));
		assertEquals(9, scn.GetStrength(lemenya));
		scn.PassSkirmishActions();

		// The Nazgûl wins and Blade Tip's response is offered
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertInZone(Zone.SUPPORT, bladetip);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bladetip));
		scn.ShadowAcceptOptionalTrigger();

		// Théoden is the loser, so it went to him: it is in the discard pile, and Hearken to Me is still his
		assertInZone(Zone.DISCARD, bladetip);
		assertAttachedTo(hearken, theoden);
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// Comprehensive Rules 5.0, "hinder": while Hearken to Me is hindered its text does not apply, so a Shadow
	// condition may be played on Théoden.  When it is restored the standing discard is in force again and the
	// condition is discarded (Ketura on Q-0223: a text with a "discard" sentence "immediately kicks in and
	// banishes other cards ... that were played on him while [it] was hindered").  The restore here is the Free
	// Peoples player's own reconcile.
	@Test
	public void AShadowConditionPlayedOnTheodenWhileItWasHinderedIsDiscardedWhenItIsRestored() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowConditionScenario();

		var hearken = scn.GetFreepsCard("hearken");
		var theoden = scn.GetFreepsCard("theoden");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		var weary = scn.GetShadowCard("weary");
		scn.MoveCompanionsToTable(theoden);
		scn.AttachCardsTo(theoden, hearken);
		// Gimli stays in hand, so that the reconcile has a discard to ask about
		scn.MoveCardsToHand(gimli, weary);

		scn.StartGame();
		scn.HinderCard(hearken);
		scn.FreepsPass();

		// Its bar is off: Théoden is offered beside Frodo
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(weary));
		scn.ShadowPlayCard(weary);
		assertTrue(scn.ShadowHasCardChoiceAvailable(theoden, frodo));
		scn.ShadowChooseCard(theoden);
		assertAttachedTo(weary, theoden);
		assertTrue(scn.IsHindered(hearken));

		// No minion: the Shadow phase is followed by the regroup phase
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassRegroupActions();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.IsHindered(hearken));
		assertAttachedTo(weary, theoden);

		scn.FreepsChooseToStay();
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		assertFalse(scn.IsHindered(hearken));
		assertAttachedTo(hearken, theoden);
		assertInZone(Zone.DISCARD, weary);
		assertInZone(Zone.HAND, gimli);
	}
}
