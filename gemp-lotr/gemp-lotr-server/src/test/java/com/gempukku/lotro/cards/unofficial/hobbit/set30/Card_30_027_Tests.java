package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_027_Tests
{

	// The Hobbit table: the Hobbit path (site 7 is Erebor 30_55, a mountain, where Smaug may stand) and Bilbo,
	// Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "30_27");
					// He gives me courage: [gandalf] maneuver event, "Spot Gandalf to remove a doubt" (one doubt is laid)
					put("courage", "30_30");
					// Take up arms. Fight!: [gandalf] skirmish event and spell, "Spot Gandalf to make a minion strength -3"
					put("take", "30_31");
					// No Finer Blade: [gandalf] skirmish event, "... or play Glamdring from your draw deck or discard pile"
					put("blade", "33_21");
					put("lore", "30_13");       // Lore of Imladris: [dwarven] maneuver event, "Heal a companion"
					put("glamdring", "30_29");  // Glamdring: [gandalf] artifact, hand weapon; "Bearer must be Gandalf"
					put("emeralds", "31_3");    // Emeralds of Girion: [dwarven] artifact for the support area; abilities only
					put("axe", "31_1");         // Dwarven Axe: a [dwarven] possession on a [dwarven] character
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion; a fellowship ability only
					put("kili", "30_12");       // Kili, Brother of Fili: [dwarven] Dwarf companion; an archery ability only
					put("dain", "32_2");        // Dáin Ironfoot: [dwarven] Dwarf ally; a regroup trigger only
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Dwarf; +2 only with 3 Ents
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, vitality 2; an assignment ability only
					put("watchful", "30_40");   // Watchful Orc: [gundabad] Orc, vitality 3; a play trigger and a Shadow ability only
					put("spider", "31_55");     // Wicked Spider: [spider] Spider, vitality 2, fierce; a skirmish ability only
					// Smaug, The Golden: [smaug] Dragon minion; discarded if not at a mountain, a regroup ability
					put("smaug", "30_66");
					// Dark Sorcery: [wraith] condition, "The first sentence of Gandalf's game text does not apply"
					put("sorcery", "33_61");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void GandalfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Gandalf, The Grey
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Wizard
		 * Strength: 7
		 * Vitality: 4
		 * Resistance: 6
		 * Game Text: <b>Wise</b>.<br>Each time you play a [gandalf] event, you may wound a minion (except Smaug).<br><b>Fellowship:</b> Add 2 doubts to play an artifact or a Dwarf companion from your draw deck or discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gandalf");

		assertEquals("Gandalf", card.getBlueprint().getTitle());
		assertEquals("The Grey", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.WISE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertNull(card.getBlueprint().getSignet());
	}

	@Test
	public void PlayingAGandalfEventOffersAWoundToAMinionOtherThanSmaug() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var balin = scn.GetFreepsCard("balin");
		var courage = scn.GetFreepsCard("courage");
		var aggressor = scn.GetShadowCard("aggressor");
		var spider = scn.GetShadowCard("spider");
		var smaug = scn.GetShadowCard("smaug");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCompanionsToTable(gandalf, balin);
		scn.MoveCardsToHand(courage);
		assertInZone(Zone.HAND, courage);
		// On to Esgaroth, and from there a second move, in the regroup phase, to Erebor.
		// Dáin, a [dwarven] ally, laid in the draw deck: Esgaroth's start-of-regroup offer is made only with such an
		// ally there (F-0804).
		scn.MoveCardsToBottomOfDeck(scn.GetFreepsCard("dain"));
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		// Esgaroth's start-of-regroup offer (an ally from the draw deck), declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();
		scn.PassRegroupActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();
		// At Erebor, a mountain: Smaug stands; the Spider is exhausted, so a wound kills it.
		assertEquals(7, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(aggressor, spider, smaug);
		scn.AddWoundsToChar(spider, 1);
		scn.AddBurdens(1);
		assertInZone(Zone.HAND, courage);
		assertEquals(1, scn.GetWoundsOn(spider));
		assertEquals(1, scn.GetBurdens());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(courage);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsAcceptOptionalTrigger();
		// Minions of two cultures and races; not Smaug, nor a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aggressor, spider));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(smaug, balin, gandalf));
		scn.FreepsChooseCard(spider);

		assertInZone(Zone.DISCARD, spider);
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(smaug));
		// He gives me courage removed its doubt.
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.DISCARD, courage);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DecliningTheOfferWoundsNoMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var courage = scn.GetFreepsCard("courage");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(courage);
		assertInZone(Zone.HAND, courage);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor);
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(aggressor));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(courage);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void EachGandalfEventPlayedOffersTheWoundAgainOnTheSameMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var courage = scn.GetFreepsCard("courage");
		var take = scn.GetFreepsCard("take");
		var watchful = scn.GetShadowCard("watchful");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(courage, take);
		assertInZone(Zone.HAND, courage, take);

		scn.StartGame();
		scn.MoveMinionsToTable(watchful);
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(watchful));

		// A maneuver event: the first wound.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(courage);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(watchful));

		// A skirmish event, a spell: the second, on the same Orc.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, watchful);
		scn.FreepsResolveSkirmish(gandalf);
		scn.FreepsPlayCard(take);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(watchful));
		// Take up arms made it strength -3: 8 - 3.
		assertEquals(5, scn.GetStrength(watchful));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ADwarvenEventAndAGandalfArtifactPlayedOfferNoWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var balin = scn.GetFreepsCard("balin");
		var lore = scn.GetFreepsCard("lore");
		var blade = scn.GetFreepsCard("blade");
		var glamdring = scn.GetFreepsCard("glamdring");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCompanionsToTable(gandalf, balin);
		scn.MoveCardsToHand(lore, blade);
		assertInZone(Zone.HAND, lore, blade);
		assertInZone(Zone.DECK, glamdring);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor);
		scn.AddWoundsToChar(balin, 1);
		assertEquals(1, scn.GetWoundsOn(balin));

		// A [dwarven] event: Lore of Imladris heals Balin, and nothing is offered.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(lore);
		assertEquals(0, scn.GetWoundsOn(balin));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// No Finer Blade, a [gandalf] event, is offered once (declined); the [gandalf] artifact it plays is not.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, aggressor);
		scn.FreepsResolveSkirmish(balin);
		scn.FreepsPlayCard(blade);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(glamdring);
		assertAttachedTo(glamdring, gandalf);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithSmaugTheOnlyMinionNoWoundIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var courage = scn.GetFreepsCard("courage");
		var smaug = scn.GetShadowCard("smaug");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(courage);
		assertInZone(Zone.HAND, courage);
		// On to Esgaroth, and from there a second move, in the regroup phase, to Erebor.
		// Dáin, a [dwarven] ally, laid in the draw deck: Esgaroth's start-of-regroup offer is made only with such an
		// ally there (F-0804).
		scn.MoveCardsToBottomOfDeck(scn.GetFreepsCard("dain"));
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		// Esgaroth's start-of-regroup offer (an ally from the draw deck), declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();
		scn.PassRegroupActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();
		assertEquals(7, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(smaug);
		scn.AddBurdens(1);
		assertInZone(Zone.HAND, courage);
		assertEquals(1, scn.GetBurdens());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(courage);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(smaug));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void FellowshipActionAddsTwoDoubtsToPlayAnArtifactFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var emeralds = scn.GetFreepsCard("emeralds");
		var axe = scn.GetFreepsCard("axe");
		var balin = scn.GetFreepsCard("balin");
		var kili = scn.GetFreepsCard("kili");
		var dain = scn.GetFreepsCard("dain");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf);
		// A Dwarf companion in the discard pile as well, so that the choice of pile is asked.
		scn.MoveCardsToDiscard(kili);
		assertInZone(Zone.DISCARD, kili);
		assertInZone(Zone.DECK, glamdring, emeralds, axe, balin, dain, avenger);

		scn.StartGame();
		assertEquals(0, scn.GetBurdens());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.FreepsActionAvailable(gandalf));
		scn.FreepsUseCardAction(gandalf);
		assertEquals(2, scn.GetBurdens());

		scn.FreepsChoose("draw deck");
		scn.FreepsDismissRevealedCards();
		// Artifacts of two cultures, borne and in the support area, and a Dwarf companion; not a possession, a
		// Dwarf ally, nor a companion who is no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(glamdring, emeralds, balin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(axe, dain, avenger));
		scn.FreepsChooseCardBPFromSelection(glamdring);

		// Gandalf is Glamdring's one bearer.
		assertAttachedTo(glamdring, gandalf);
		assertInZone(Zone.DECK, emeralds, balin);
		assertInZone(Zone.DISCARD, kili);
		// Shuffled as the card leaves the deck looked through, and again after the play.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionAddsTwoDoubtsToPlayADwarfCompanionFromHisOwnDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var kili = scn.GetFreepsCard("kili");
		var emeralds = scn.GetFreepsCard("emeralds");
		var axe = scn.GetFreepsCard("axe");
		var dain = scn.GetFreepsCard("dain");
		var avenger = scn.GetFreepsCard("avenger");
		var balin = scn.GetFreepsCard("balin");
		var shadowBalin = scn.GetShadowCard("balin");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToDiscard(kili, emeralds, axe, dain, avenger, shadowBalin);
		// The other player's Balin lies in his discard pile; this player's own is in hand, in neither pile.
		scn.MoveCardsToHand(balin);
		assertInZone(Zone.DISCARD, kili, emeralds, axe, dain, avenger, shadowBalin);
		assertInZone(Zone.HAND, balin);

		scn.StartGame();
		assertEquals(0, scn.GetBurdens());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		scn.FreepsUseCardAction(gandalf);
		assertEquals(2, scn.GetBurdens());

		scn.FreepsChoose("discard pile");
		// A Dwarf companion and an artifact from his own discard pile; not a possession, a Dwarf ally, a companion
		// who is no Dwarf, nor the other player's Dwarf companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(kili, emeralds));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(axe, dain, avenger, shadowBalin));
		scn.FreepsChooseCard(kili);

		assertInZone(Zone.FREE_CHARACTERS, kili);
		assertInZone(Zone.DISCARD, emeralds, shadowBalin);
		// No look at the draw deck.
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNothingToPlayInTheDiscardPileTheDrawDeckIsLookedThroughUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var axe = scn.GetFreepsCard("axe");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToDiscard(axe);
		assertInZone(Zone.DISCARD, axe);
		assertInZone(Zone.DECK, balin);

		scn.StartGame();
		scn.FreepsUseCardAction(gandalf);
		assertEquals(2, scn.GetBurdens());

		// The discard pile holds only a possession: the deck is the pile, and no choice of piles is asked.
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(balin);
		assertInZone(Zone.FREE_CHARACTERS, balin);
		assertInZone(Zone.DISCARD, axe);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionIsNotOfferedWithNothingToPlayInEitherPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var axe = scn.GetFreepsCard("axe");
		var dain = scn.GetFreepsCard("dain");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf);
		scn.SetFreepsDrawDeck(dain, avenger);
		scn.MoveCardsToDiscard(axe);
		assertInZone(Zone.DECK, dain, avenger);
		assertInZone(Zone.DISCARD, axe);

		scn.StartGame();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gandalf));
	}

	@Test
	public void FellowshipActionIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var balin = scn.GetFreepsCard("balin");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCompanionsToTable(gandalf);
		assertInZone(Zone.DECK, balin);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gandalf));
	}

	@Test
	public void WithHisFirstSentenceMadeNotToApplyByDarkSorceryAGandalfEventOffersNoWoundAndHisFellowshipActionIsStillOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var courage = scn.GetFreepsCard("courage");
		var aggressor = scn.GetShadowCard("aggressor");
		var sorcery = scn.GetShadowCard("sorcery");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(courage);
		scn.MoveCardsToSupportArea(sorcery);
		assertInZone(Zone.HAND, courage);
		assertInZone(Zone.SUPPORT, sorcery);

		scn.StartGame();
		// Dark Sorcery leaves his Fellowship action alone.
		assertTrue(scn.FreepsActionAvailable(gandalf));

		scn.MoveMinionsToTable(aggressor);
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(aggressor));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(courage);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
