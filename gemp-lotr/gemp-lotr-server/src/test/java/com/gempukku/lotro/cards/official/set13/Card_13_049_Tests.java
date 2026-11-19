package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_049_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("homestead", "13_49");
					// Smeagol, Slinker: a [gollum] Smeagol companion (to play, add a burden)
					put("smeagol", "5_29");
					// Smeagol, Once Like Us: a [shire] Smeagol companion; never in play beside the other
					put("shiresmeagol", "103_116");
					// Deagol, Fateful Finder: a [gollum] follower of another title (to play, spot Smeagol)
					put("deagol", "13_46");
					// Fishing Boat: a [gollum] possession Smeagol bears; cheated on, so its "when you play" is not asked
					put("boat", "13_48");
					// Unabated in Malice: a Shadow [gollum] condition in a support area; its texts are about [gollum]
					// minions and a skirmish
					put("unabated", "10_24");
					// Safe Passage: a Free Peoples [gollum] condition in a support area
					put("passage", "11_50");
					// Weary: an [isengard] condition the Shadow player plays on a companion
					put("weary", "4_212");
					// Hemmed In: a [men] condition in the Shadow support area; its ability needs a [men] minion
					put("hemmed", "12_70");
					// Spider Poison: a Shadow possession in the Shadow support area, a Shadow card that is no condition
					put("poison", "8_28");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetManeuverScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("homestead", "13_49");
					// Saruman's Ambition: an [isengard] condition in the Shadow support area; its texts are about
					// [isengard] events and a skirmish
					put("ambition", "1_133");
					// Lingering Shadow: a [wraith] condition in the Shadow support area; its ability needs a skirmish
					put("shadow", "12_166");
					// Ships of Great Draught: a [raider] possession in the Shadow support area, a Shadow card that
					// is no condition; a Shadow and a Regroup ability
					put("ships", "8_65");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
					put("runner", "1_178");     // Goblin Runner: the known card on top of the Shadow draw deck
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GladdenHomesteadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Gladden Homestead
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot Sméagol.
		 * When you play this, add a [gollum] token here for each [gollum] card you can spot.
		 * <b>Maneuver:</b> Discard this from play or remove 2 tokens from here to spot a Shadow condition and place that condition on top of its owner's draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("homestead");

		assertEquals("Gladden Homestead", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilSmeagolIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var smeagol = scn.GetFreepsCard("smeagol");
		var deagol = scn.GetFreepsCard("deagol");
		scn.MoveCardsToSupportArea(deagol);
		scn.MoveCardsToHand(homestead, smeagol);

		scn.StartGame();

		// Deagol is a [gollum] card of another title.
		assertInZone(Zone.HAND, homestead);
		assertFalse(scn.FreepsPlayAvailable(homestead));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(smeagol);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);

		assertTrue(scn.FreepsPlayAvailable(homestead));
		scn.FreepsPlayCard(homestead);
		assertInZone(Zone.SUPPORT, homestead);
		// Smeagol, Deagol and the Homestead itself.
		assertEquals(3, scn.GetCultureTokensOn(homestead));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedWithSmeagolAddsATokenForEachGollumCardThatCanBeSpottedItselfIncluded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var smeagol = scn.GetFreepsCard("smeagol");
		var boat = scn.GetFreepsCard("boat");
		var deagol = scn.GetFreepsCard("deagol");
		var passage = scn.GetFreepsCard("passage");
		// The first player's own Unabated in Malice, a Shadow card of his, inactive on his turn.
		var myUnabated = scn.GetFreepsCard("unabated");
		var unabated = scn.GetShadowCard("unabated");
		scn.MoveCompanionsToTable(smeagol);
		scn.AttachCardsTo(smeagol, boat);
		scn.MoveCardsToSupportArea(deagol, myUnabated, unabated);
		scn.MoveCardsToHand(homestead, passage);

		scn.StartGame();

		scn.FreepsPlayCard(homestead);

		// Smeagol, his Fishing Boat, Deagol, the other player's Unabated in Malice and the Homestead itself.  Not
		// the first player's own Shadow condition, the [gollum] condition in hand, or the [shire] Ring-bearer.
		assertInZone(Zone.SUPPORT, homestead);
		assertEquals(5, scn.GetCultureTokensOn(homestead));
		assertInZone(Zone.HAND, passage);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedWithTheShireSmeagolAloneAddsOneTokenForItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var smeagol = scn.GetFreepsCard("shiresmeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(homestead);

		scn.StartGame();

		// A Smeagol who is a [shire] card meets the requirement and is not counted.
		assertTrue(scn.FreepsPlayAvailable(homestead));
		scn.FreepsPlayCard(homestead);

		assertInZone(Zone.SUPPORT, homestead);
		assertEquals(1, scn.GetCultureTokensOn(homestead));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AGollumCardPlayedAfterTheHomesteadAddsNoToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var smeagol = scn.GetFreepsCard("smeagol");
		var deagol = scn.GetFreepsCard("deagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(homestead, deagol);

		scn.StartGame();

		scn.FreepsPlayCard(homestead);
		// Smeagol and the Homestead itself.
		assertEquals(2, scn.GetCultureTokensOn(homestead));

		scn.FreepsPlayCard(deagol);
		assertInZone(Zone.SUPPORT, deagol);
		assertEquals(2, scn.GetCultureTokensOn(homestead));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RemovesTwoTokensToPlaceAShadowConditionBorneByACompanionOnTopOfItsOwnersDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var homestead = scn.GetFreepsCard("homestead");
		var passage = scn.GetFreepsCard("passage");
		scn.MoveCardsToSupportArea(homestead, passage);
		scn.AddTokensToCard(homestead, 3);

		var weary = scn.GetShadowCard("weary");
		var hemmed = scn.GetShadowCard("hemmed");
		var poison = scn.GetShadowCard("poison");
		scn.AttachCardsTo(frodo, weary);
		scn.MoveCardsToSupportArea(hemmed, poison);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(3, scn.GetCultureTokensOn(homestead));
		assertTrue(scn.GetAttachedCards(frodo).contains(weary));
		assertInZone(Zone.SUPPORT, hemmed, poison, passage);
		int freepsDeck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();

		assertTrue(scn.FreepsActionAvailable(homestead));
		scn.FreepsUseCardAction(homestead);

		// With two tokens or more, either cost may be paid.
		assertEquals(List.of("Discard this from play", "Remove 2 tokens from here"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Remove 2 tokens from here");
		assertEquals(1, scn.GetCultureTokensOn(homestead));
		assertInZone(Zone.SUPPORT, homestead);

		// A Shadow condition on a companion and one in the support area.  Not a Shadow possession, nor a Free
		// Peoples condition.
		assertTrue(scn.FreepsHasCardChoiceAvailable(weary, hemmed));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(poison, passage, homestead));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(weary);

		assertSame(weary, scn.GetShadowTopOfDeck());
		assertFalse(scn.GetAttachedCards(frodo).contains(weary));
		assertEquals(shadowDeck + 1, scn.GetShadowDeckCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertInZone(Zone.SUPPORT, hemmed, poison, passage);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithOneTokenItIsDiscardedToPayAndNothingIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		scn.MoveCardsToSupportArea(homestead);
		scn.AddTokensToCard(homestead, 1);

		var weary = scn.GetShadowCard("weary");
		var hemmed = scn.GetShadowCard("hemmed");
		scn.AttachCardsTo(scn.GetRingBearer(), weary);
		scn.MoveCardsToSupportArea(hemmed);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetCultureTokensOn(homestead));
		assertTrue(scn.FreepsActionAvailable(homestead));
		scn.FreepsUseCardAction(homestead);

		// Two tokens cannot be removed: the Homestead is discarded and the condition is chosen at once.
		assertInDiscard(homestead);
		assertTrue(scn.FreepsHasCardChoiceAvailable(weary, hemmed));
		scn.FreepsChooseCard(hemmed);

		assertSame(hemmed, scn.GetShadowTopOfDeck());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoShadowConditionItIsStillUsedAndPlacesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		scn.MoveCardsToSupportArea(homestead);
		scn.AddTokensToCard(homestead, 3);

		var poison = scn.GetShadowCard("poison");
		scn.MoveCardsToSupportArea(poison);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(3, scn.GetCultureTokensOn(homestead));
		assertInZone(Zone.SUPPORT, poison);
		int shadowDeck = scn.GetShadowDeckCount();

		// The spot stands after "to": it is part of the effect, and the cost is paid for nothing.
		assertTrue(scn.FreepsActionAvailable(homestead));
		scn.FreepsUseCardAction(homestead);
		scn.FreepsChoose("Remove 2 tokens from here");

		assertEquals(1, scn.GetCultureTokensOn(homestead));
		assertInZone(Zone.SUPPORT, poison);
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		scn.MoveCardsToSupportArea(homestead);
		scn.AddTokensToCard(homestead, 3);

		var hemmed = scn.GetShadowCard("hemmed");
		scn.MoveCardsToSupportArea(hemmed);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(homestead));
		assertEquals(3, scn.GetCultureTokensOn(homestead));
		assertInZone(Zone.SUPPORT, hemmed);

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(homestead));
		assertEquals(3, scn.GetCultureTokensOn(homestead));
		assertInZone(Zone.SUPPORT, hemmed);
	}

	@Test
	public void DiscardedToPlaceAShadowConditionOnTopOfItsOwnersDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetManeuverScenario();

		// In play with no token on it, so that discarding it is the only way to pay.
		var homestead = scn.GetFreepsCard("homestead");
		scn.MoveCardsToSupportArea(homestead);

		var ambition = scn.GetShadowCard("ambition");
		var shadow = scn.GetShadowCard("shadow");
		var ships = scn.GetShadowCard("ships");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(ambition, shadow, ships);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(runner);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetCultureTokensOn(homestead));
		assertInZone(Zone.SUPPORT, ambition, shadow, ships);
		assertEquals(runner, scn.GetShadowTopOfDeck());
		int freepsDeck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();

		// Nothing is asked about the cost: the Homestead is discarded, and the condition is then chosen.
		assertTrue(scn.FreepsActionAvailable(homestead));
		scn.FreepsUseCardAction(homestead);
		assertInDiscard(homestead);
		assertTrue(scn.FreepsHasCardChoiceAvailable(ambition, shadow));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ships));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(ambition);

		// It lies on top of its owner's draw deck, the card that was on top now under it.
		assertEquals(ambition, scn.GetShadowTopOfDeck());
		assertEquals(runner, scn.GetFromTopOfShadowDeck(2));
		assertEquals(shadowDeck + 1, scn.GetShadowDeckCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertInZone(Zone.SUPPORT, shadow, ships);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
