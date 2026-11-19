package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_181_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mordor", "13_181");
					put("mordor2", "13_181");

					// Cards for the support area, and beside it
					put("destroyed", "1_220");   // Not Easily Destroyed: [wraith] support-area condition, cost 1; an ability used only on purpose
					put("crown", "101_39");      // Pale Crown: [wraith] support-area artifact; its text reaches only a bearer
					put("spires", "1_249");      // Gleaming Spires Will Crumble: [sauron] support-area condition; skirmishes only

					// Minions to bear a mount
					put("otsea", "12_179");      // Úlairë Otsëa, Black Specter: [wraith] Nazgûl; a Shadow ability used only on purpose
					put("lemenya", "1_232");     // Úlairë Lemenya, Lieutenant of Morgul: [wraith] Nazgûl; her text reaches bearers of [wraith] conditions only
					put("khamul", "103_61");     // Úlairë Attëa, Khamûl of the East: a [raider] Nazgûl; the rest needs 3 burdens

					// Cards for the draw deck
					put("steed", "1_208");       // Black Steed: [wraith] mount, cost 1, bearer must be a Nazgûl; strength +2 at a plains
					put("smordor", "12_172");    // Steed of Mordor: [wraith] mount, cost 2, bearer must be a Nazgûl; muster, and a draw when he wins
					put("blade", "12_168");      // Nazgûl Blade: [wraith] hand weapon, cost 1, bearer must be a Nazgûl: a possession that is no mount
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRedhornScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mordor", "13_181");
					put("destroyed", "1_220");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					// Redhorn Pass: a Shadows-block mountain, laid by MoveCardToAdventurePath as site 2; "Culture tokens
					// cannot be added, removed, or reinforced."
					put("redhorn", "13_194");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheyCameFromMordorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: They Came From Mordor
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: When you play this, you may add a [wraith] token here for each [wraith] card in your support area.<br><b>Shadow:</b> Discard this from play or remove 2 tokens from here to play a [wraith] mount from your draw deck on your [wraith] minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mordor");

		assertEquals("They Came From Mordor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedAloneItCountsItselfForOneTokenAndAnotherWraithConditionPlayedLaterAddsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mordor = scn.GetShadowCard("mordor");
		var destroyed = scn.GetShadowCard("destroyed");
		scn.MoveCardsToHand(mordor, destroyed);

		scn.StartGame();
		assertInHand(mordor, destroyed);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(mordor));
		scn.ShadowPlayCard(mordor);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mordor));
		scn.ShadowAcceptOptionalTrigger();

		// The only [wraith] card in the support area is this one.
		assertEquals(1, scn.GetCultureTokensOn(mordor));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another [wraith] condition played afterwards does not set it off again.
		scn.ShadowPlayCard(destroyed);
		assertInZone(Zone.SUPPORT, destroyed);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(mordor));
		assertEquals(1, scn.GetCultureTokensOn(mordor));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TakesATokenForEachWraithCardInYourSupportAreaAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mordor = scn.GetShadowCard("mordor");
		var mordor2 = scn.GetShadowCard("mordor2");
		var destroyed = scn.GetShadowCard("destroyed");
		var crown = scn.GetShadowCard("crown");
		var spires = scn.GetShadowCard("spires");
		var otsea = scn.GetShadowCard("otsea");
		var steed = scn.GetShadowCard("steed");
		scn.MoveCardsToHand(mordor);
		scn.MoveCardsToSupportArea(mordor2, destroyed, crown, spires);

		scn.StartGame();
		scn.MoveMinionsToTable(otsea);
		scn.AttachCardsTo(otsea, steed);
		assertAttachedTo(steed, otsea);
		assertInZone(Zone.SUPPORT, mordor2, destroyed, crown, spires);
		assertEquals(0, scn.GetCultureTokensOn(mordor2));
		scn.SetTwilight(3);
		scn.FreepsPass();

		scn.ShadowPlayCard(mordor);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mordor));
		scn.ShadowAcceptOptionalTrigger();

		// Itself, the other copy, the [wraith] condition and the [wraith] artifact; not the [sauron] condition there,
		// nor the [wraith] Nazgûl in play or the [wraith] mount he bears. The tokens go on this card alone.
		assertEquals(4, scn.GetCultureTokensOn(mordor));
		assertEquals(0, scn.GetCultureTokensOn(mordor2));
		assertEquals(0, scn.GetCultureTokensOn(destroyed));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferAddsNoToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mordor = scn.GetShadowCard("mordor");
		var destroyed = scn.GetShadowCard("destroyed");
		scn.MoveCardsToHand(mordor);
		scn.MoveCardsToSupportArea(destroyed);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, destroyed);
		scn.SetTwilight(3);
		scn.FreepsPass();

		scn.ShadowPlayCard(mordor);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mordor));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, mordor);
		assertEquals(0, scn.GetCultureTokensOn(mordor));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhereNoTokenCanBeAddedNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRedhornScenario();

		var mordor = scn.GetShadowCard("mordor");
		var destroyed = scn.GetShadowCard("destroyed");
		var redhorn = scn.GetShadowSite("redhorn");
		scn.MoveCardsToHand(mordor);
		scn.MoveCardsToSupportArea(destroyed);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, destroyed);
		scn.MoveCardToAdventurePath(redhorn);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertSame(redhorn, scn.GetCurrentSite());
		scn.ShadowPlayCard(mordor);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(mordor));
		assertInZone(Zone.SUPPORT, mordor);
		assertEquals(0, scn.GetCultureTokensOn(mordor));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithFewerThan2TokensItIsDiscardedToPlayAWraithMountFromTheDrawDeckOnAWraithMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mordor = scn.GetShadowCard("mordor");
		var mordor2 = scn.GetShadowCard("mordor2");
		var otsea = scn.GetShadowCard("otsea");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var steed = scn.GetShadowCard("steed");
		var smordor = scn.GetShadowCard("smordor");
		var blade = scn.GetShadowCard("blade");
		scn.MoveCardsToSupportArea(mordor, mordor2);
		// The other copy holds 2 tokens; this one holds none.
		scn.AddTokensToCard(mordor2, 2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, mordor, mordor2);
		assertEquals(0, scn.GetCultureTokensOn(mordor));
		assertEquals(2, scn.GetCultureTokensOn(mordor2));
		scn.MoveCardsToTopOfDeck(steed, smordor, blade);
		assertInZone(Zone.DECK, steed, smordor, blade);
		scn.MoveMinionsToTable(otsea, lemenya, khamul);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(mordor));
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		scn.ShadowUseCardAction(mordor);

		// The discard is the only way to pay, and is taken with nothing asked; the other copy keeps its tokens.
		assertInDiscard(mordor);
		assertInZone(Zone.SUPPORT, mordor2);
		assertEquals(2, scn.GetCultureTokensOn(mordor2));

		// The deck is shown, then either [wraith] mount may be chosen, and not the Blade, which is no mount.
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(steed, smordor));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(blade));
		scn.ShadowChooseCardBPFromSelection(smordor);
		// Either [wraith] Nazgûl may bear it; not the [raider] one.
		assertTrue(scn.ShadowHasCardChoiceAvailable(otsea, lemenya));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(khamul));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(lemenya);

		assertAttachedTo(smordor, lemenya);
		assertEquals(0, scn.GetAttachedCards(otsea).size());
		assertEquals(0, scn.GetAttachedCards(khamul).size());
		assertInZone(Zone.DECK, steed, blade);
		// The deck looked through is shuffled: once as the mount leaves it, once as the look is over.
		assertEquals(shadowShuffles + 2, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithBothCostsPayableThePlayerChoosesAndRemoving2TokensKeepsTheCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mordor = scn.GetShadowCard("mordor");
		var otsea = scn.GetShadowCard("otsea");
		var steed = scn.GetShadowCard("steed");
		scn.MoveCardsToSupportArea(mordor);
		scn.AddTokensToCard(mordor, 3);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, mordor);
		assertEquals(3, scn.GetCultureTokensOn(mordor));
		scn.MoveCardsToTopOfDeck(steed);
		assertInZone(Zone.DECK, steed);
		scn.MoveMinionsToTable(otsea);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(mordor));
		scn.ShadowUseCardAction(mordor);

		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		assertTrue(scn.ShadowChoiceAvailable("Discard this from play"));
		assertTrue(scn.ShadowChoiceAvailable("Remove 2 tokens"));
		scn.ShadowChooseOption("Remove 2 tokens");

		assertInZone(Zone.SUPPORT, mordor);
		assertEquals(1, scn.GetCultureTokensOn(mordor));

		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(steed);
		// One [wraith] minion: the steed goes on him with nothing asked.
		assertAttachedTo(steed, otsea);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithBothCostsPayableTheDiscardMayBeChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mordor = scn.GetShadowCard("mordor");
		var otsea = scn.GetShadowCard("otsea");
		var steed = scn.GetShadowCard("steed");
		scn.MoveCardsToSupportArea(mordor);
		scn.AddTokensToCard(mordor, 2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, mordor);
		assertEquals(2, scn.GetCultureTokensOn(mordor));
		scn.MoveCardsToTopOfDeck(steed);
		assertInZone(Zone.DECK, steed);
		scn.MoveMinionsToTable(otsea);
		scn.SetTwilight(5);
		scn.FreepsPass();

		scn.ShadowUseCardAction(mordor);
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChooseOption("Discard this from play");

		assertInDiscard(mordor);

		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(steed);
		assertAttachedTo(steed, otsea);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoWraithMountInTheDrawDeckItIsStillUsedAndPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mordor = scn.GetShadowCard("mordor");
		var otsea = scn.GetShadowCard("otsea");
		var steed = scn.GetShadowCard("steed");
		var smordor = scn.GetShadowCard("smordor");
		var blade = scn.GetShadowCard("blade");
		scn.MoveCardsToSupportArea(mordor);
		// Every [wraith] mount the deck could hold is in the discard pile; the Blade is in the deck.
		scn.MoveCardsToDiscard(steed, smordor);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, mordor);
		assertInDiscard(steed, smordor);
		scn.MoveCardsToTopOfDeck(blade);
		assertInZone(Zone.DECK, blade);
		scn.MoveMinionsToTable(otsea);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// No penalty for finding nothing in the deck (CR 4.2 "draw deck"): it is offered.
		assertTrue(scn.ShadowActionAvailable(mordor));
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		scn.ShadowUseCardAction(mordor);

		assertInDiscard(mordor);
		// The deck is shown and nothing in it can be chosen.
		assertTrue(scn.ShadowHasCardChoiceAvailable(blade, false));
		scn.ShadowDismissRevealedCards();

		assertInZone(Zone.DECK, blade);
		assertEquals(0, scn.GetAttachedCards(otsea).size());
		// The look alone shuffles the deck once.
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mordor = scn.GetShadowCard("mordor");
		var otsea = scn.GetShadowCard("otsea");
		var steed = scn.GetShadowCard("steed");
		scn.MoveCardsToSupportArea(mordor);
		scn.AddTokensToCard(mordor, 2);

		scn.StartGame();
		assertEquals(2, scn.GetCultureTokensOn(mordor));
		scn.MoveCardsToTopOfDeck(steed);
		assertInZone(Zone.DECK, steed);
		scn.MoveMinionsToTable(otsea);
		scn.SetTwilight(5);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mordor));
	}
}
