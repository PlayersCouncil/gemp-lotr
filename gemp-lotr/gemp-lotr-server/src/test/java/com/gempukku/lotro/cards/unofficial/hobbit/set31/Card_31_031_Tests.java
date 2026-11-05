package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_031_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  The Shadow phase of
	// turn 1 is at The Troll Hoard 30_50 (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("scimitar", "31_31");
					put("scimitar2", "31_31");

					put("footman", "30_42");    // Goblin Footman: [moria] Orc; a "when you play" offer, not reached (cheated in)
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc; an Assignment ability not used
					put("bats", "32_30");       // Dark Bats: [moria] Creature, no Orc; a Shadow ability not used
					put("worms", "32_33");      // Were-worms: [moria] condition, cost 0; a skirmish-win trigger, a Shadow ability at a battleground
					put("warg", "32_32");       // Vicious Warg: [moria] mount on an Orc; makes him fierce
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void GoblinScimitarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Goblin Scimitar
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Game Text: To play, spot a [moria] card.
		 * Bearer must be an Orc.
		 * When you play this possession, you may draw a card.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("scimitar");

		assertEquals("Goblin Scimitar", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void CannotBePlayedUntilAMoriaCardIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scimitar = scn.GetShadowCard("scimitar");
		var aggressor = scn.GetShadowCard("aggressor");
		var worms = scn.GetShadowCard("worms");
		scn.MoveCardsToHand(scimitar, worms);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor);
		scn.FreepsPass();

		// Only a [gundabad] Orc in play, and Were-worms in hand, which is not spotted there.
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(scimitar));

		// A [moria] condition played: a [moria] card that is neither a character nor an item.
		scn.ShadowPlayCard(worms);
		assertInZone(Zone.SUPPORT, worms);
		assertTrue(scn.ShadowPlayAvailable(scimitar));
		scn.ShadowPlayCard(scimitar);

		// The Aggressor is its only bearer, so nothing is asked; the draw is offered.
		assertAttachedTo(scimitar, aggressor);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scimitar));
	}

	@Test
	public void CanBePlayedWhileOnlyAMoriaPossessionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scimitar = scn.GetShadowCard("scimitar");
		var aggressor = scn.GetShadowCard("aggressor");
		var warg = scn.GetShadowCard("warg");
		scn.MoveCardsToHand(scimitar);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor);
		scn.AttachCardsTo(aggressor, warg);
		assertAttachedTo(warg, aggressor);
		scn.FreepsPass();

		// The [moria] mount on the [gundabad] Orc is the only [moria] card in play.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(scimitar));
	}

	@Test
	public void PlaysOnAnOrcOfAnyCultureAndMayDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scimitar = scn.GetShadowCard("scimitar");
		var footman = scn.GetShadowCard("footman");
		var aggressor = scn.GetShadowCard("aggressor");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCardsToHand(scimitar);

		scn.StartGame();
		scn.MoveMinionsToTable(footman, aggressor, bats);
		scn.FreepsPass();

		// The [moria] cards spotted are characters alone: the Footman and the Bats.
		assertTrue(scn.ShadowPlayAvailable(scimitar));
		scn.ShadowPlayCard(scimitar);

		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(footman, aggressor));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bats));
		scn.ShadowChooseCard(aggressor);
		assertAttachedTo(scimitar, aggressor);

		var hand = scn.GetShadowHandCount();
		var deck = scn.GetShadowDeckCount();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scimitar));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheDrawLeavesHandAndDeckAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scimitar = scn.GetShadowCard("scimitar");
		var footman = scn.GetShadowCard("footman");
		scn.MoveCardsToHand(scimitar);

		scn.StartGame();
		scn.MoveMinionsToTable(footman);
		scn.FreepsPass();

		scn.ShadowPlayCard(scimitar);
		assertAttachedTo(scimitar, footman);

		var hand = scn.GetShadowHandCount();
		var deck = scn.GetShadowDeckCount();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scimitar));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(hand, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACopyAlreadyInPlayDoesNotAnswerTheSecondCopysPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scimitar = scn.GetShadowCard("scimitar");
		var scimitar2 = scn.GetShadowCard("scimitar2");
		var footman = scn.GetShadowCard("footman");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCardsToHand(scimitar2);

		scn.StartGame();
		scn.MoveMinionsToTable(footman, aggressor);
		scn.AttachCardsTo(footman, scimitar);
		assertAttachedTo(scimitar, footman);
		scn.FreepsPass();

		var hand = scn.GetShadowHandCount();
		var deck = scn.GetShadowDeckCount();

		// The Footman already bears a hand weapon, so the second copy goes on the Aggressor unasked.
		scn.ShadowPlayCard(scimitar2);
		assertAttachedTo(scimitar2, aggressor);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scimitar2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(scimitar));
		scn.ShadowAcceptOptionalTrigger();

		// One card drawn: the copy played, and not the copy in play, answers.  The hand lost the copy played.
		assertEquals(hand - 1 + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
