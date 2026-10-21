package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_011_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("smeagol", "19_11");
					put("sam", "1_311");          // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer; abilities used only on purpose
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: an unbound companion; a maneuver ability never used

					put("gollum", "5_24");        // Gollum, Nasty Treacherous Creature: a [gollum] minion (Shadow), left in the discard pile
					put("lookat", "6_39");        // Don't Look at Them: a [gollum] condition (Free Peoples), left in the discard pile
					put("notlistening", "6_43");  // Not Listening: a [gollum] event (Free Peoples), left in the discard pile

					put("savage", "1_151");       // Uruk Savage: [isengard], strength 5, vitality 3; damage +1 is its whole text
					put("seeker", "4_195");       // Uruk Seeker: fierce, strength 6, vitality 2; its bonus is against a bearer of a search card only
					put("slayer", "3_97");        // Orc Slayer: [sauron] Orc, strength 5, vitality 2; a regroup ability never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SmeagolStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Sméagol, Pitiable Guide
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype:
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 5
		 * Game Text: Ring-bound. To play, add a burden.
		 * 	Each time Sméagol wins a skirmish, you may shuffle up to 2 [gollum] cards from your discard pile into your draw deck.
		 * 	Skirmish: Exert a Ring-bound companion to make Sméagol strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("smeagol");

		assertEquals("Sméagol", card.getBlueprint().getTitle());
		assertEquals("Pitiable Guide", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
	}

	@Test
	public void PlayingSmeagolAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCardsToHand(smeagol);

		scn.StartGame();

		assertInZone(Zone.HAND, smeagol);
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.FreepsPlayAvailable(smeagol));

		scn.FreepsPlayCard(smeagol);

		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertEquals(1, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeSmeagolWinsUpTo2GollumCardsMayBeShuffledFromTheDiscardPileIntoTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var gollum = scn.GetFreepsCard("gollum");
		var lookat = scn.GetFreepsCard("lookat");
		var notlistening = scn.GetFreepsCard("notlistening");
		var discardedSavage = scn.GetFreepsCard("savage");
		var seeker = scn.GetShadowCard("seeker");
		scn.MoveCompanionsToTable(smeagol);
		// Three [gollum] cards of three types and both sides, and a card of another culture.
		scn.MoveCardsToDiscard(gollum, lookat, notlistening, discardedSavage);
		assertInDiscard(gollum, lookat, notlistening, discardedSavage);

		scn.StartGame();
		// Sméagol 3 + 4: he beats Uruk Seeker (6) without killing him.
		scn.ApplyAdHocModifier(new StrengthModifier(null, smeagol, null, 4));
		scn.FreepsPass();
		scn.MoveMinionsToTable(seeker);


		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, seeker);
		scn.FreepsResolveSkirmish(smeagol);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(seeker));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(smeagol));
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsDeclineOptionalTrigger();
		assertInDiscard(gollum, lookat, notlistening, discardedSavage);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());

		// The Seeker is fierce: Sméagol beats him a second time, and the offer is made again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(smeagol, seeker);
		scn.FreepsResolveSkirmish(smeagol);
		scn.PassFierceSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(smeagol));

		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(gollum, lookat, notlistening));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(discardedSavage));
		assertEquals(0, scn.FreepsGetChoiceMin());
		// Three could be shuffled in; two is the most.
		assertEquals(2, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCards(gollum, lookat);

		assertInZone(Zone.DECK, gollum, lookat);
		assertInDiscard(notlistening, discardedSavage);
		assertEquals(deck + 2, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoGollumCardInTheDiscardPileTheWinStillOffersTheShuffle() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var discardedSavage = scn.GetFreepsCard("savage");
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToDiscard(discardedSavage);
		assertInDiscard(discardedSavage);

		scn.StartGame();
		// Sméagol 3 + 4 beats Orc Slayer (5) without killing him.
		scn.ApplyAdHocModifier(new StrengthModifier(null, smeagol, null, 4));
		scn.FreepsPass();
		scn.MoveMinionsToTable(slayer);


		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, slayer);
		scn.FreepsResolveSkirmish(smeagol);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(slayer));
		// Shuffling up to 2 cards in is a shuffle of the deck whatever is shuffled in (Q-0102).
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(smeagol));
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsAcceptOptionalTrigger();

		assertInDiscard(discardedSavage);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenSmeagolLosesASkirmishNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var gollum = scn.GetFreepsCard("gollum");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToDiscard(gollum);
		assertInDiscard(gollum);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);


		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, savage);
		scn.FreepsResolveSkirmish(smeagol);

		assertEquals(0, scn.GetWoundsOn(smeagol));
		scn.PassSkirmishActions();

		// Uruk Savage (5) beats Sméagol (3): one wound and one for damage +1, and he lives.
		assertEquals(2, scn.GetWoundsOn(smeagol));
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInDiscard(gollum);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsARingBoundCompanionToMakeSmeagolStrongerUntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCompanionsToTable(smeagol, sam, aragorn);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage, slayer);

		scn.SkipToAssignments();
		// Sméagol is assigned to nobody.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, savage }, new PhysicalCardImpl[] { sam, slayer });
		scn.FreepsResolveSkirmish(aragorn);

		assertFalse(scn.IsCharAssigned(smeagol));
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsActionAvailable(smeagol));

		scn.FreepsUseCardAction(smeagol);
		// Every Ring-bound companion, Sméagol himself among them; not the unbound Aragorn.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, smeagol));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));

		scn.FreepsChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(5, scn.GetStrength(smeagol));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// Sam's skirmish, the next: the bonus is gone with Aragorn's.
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetStrength(smeagol));
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhenEveryRingBoundCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(smeagol, sam, aragorn);

		scn.StartGame();
		// Vitality 4 each: three wounds leave each of them exhausted.
		scn.AddWoundsToChar(smeagol, 3);
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(sam, 3);

		assertEquals(3, scn.GetWoundsOn(smeagol));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, savage);
		scn.FreepsResolveSkirmish(smeagol);

		// Aragorn could exert, but he is not Ring-bound.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(smeagol));
	}
}
