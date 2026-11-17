package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_002_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 (vitality 4) as the Ring-bearer, so
	// "Bilbo" is always the Ring-bearer here. Turn 1 begins at Bag End (site 1).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("service", "33_2");

					// Cards it may play: [dwarven] possessions, one borne and one for the support area, and [dwarven]
					// followers of two races
					put("axe", "31_1");             // Dwarven Axe: [dwarven] hand weapon, bearer a [dwarven] character
					put("chariot", "33_10");        // Iron Hills Chariot: [dwarven] support-area possession; its texts want Balin to exert
					put("bifur", "30_5");           // Bifur, Inarticulate: [dwarven] Dwarf follower
					put("roac", "32_8");            // Roäc, Son of Carc: [dwarven] Bird follower
					// Cards it may not play
					put("arkenstone", "31_7");      // The Arkenstone: a [dwarven] artifact (support area), no possession
					put("grimir", "1_17");          // Grimir, Dwarven Elder: a [dwarven] ally
					put("ironforged", "32_18");     // Iron-Forged Weapon: an [esgaroth] possession a [dwarven] companion may bear
					put("thrush", "31_42");         // Old Thrush, Messenger Bird: a [shire] follower

					put("balin", "30_2");           // Balin, Brother of Dwalin: [dwarven] companion, to bear a weapon; his ability not used
					put("merry", "1_303");          // Merry, From O'er the Brandywine: a [shire] Hobbit companion who is no Bilbo
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void AtyourServiceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: At your Service!
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Exert Bilbo and add a doubt to play a [dwarven] possession or a [dwarven] follower from your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("service");

		assertEquals("At your Service!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsBilboAndAddsADoubtToPlayADwarvenPossessionFromTheDrawDeckOntoABearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var service = scn.GetFreepsCard("service");
		var axe = scn.GetFreepsCard("axe");
		var chariot = scn.GetFreepsCard("chariot");
		var bifur = scn.GetFreepsCard("bifur");
		var roac = scn.GetFreepsCard("roac");
		var arkenstone = scn.GetFreepsCard("arkenstone");
		var grimir = scn.GetFreepsCard("grimir");
		var ironforged = scn.GetFreepsCard("ironforged");
		var thrush = scn.GetFreepsCard("thrush");
		var balin = scn.GetFreepsCard("balin");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToHand(service);

		scn.StartGame();

		assertInZone(Zone.DECK, axe, chariot, bifur, roac, arkenstone, grimir, ironforged, thrush);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetBurdens());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.FreepsPlayAvailable(service));

		scn.FreepsPlayCard(service);
		// Bilbo exerts, and Balin, who could, does not.
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(1, scn.GetBurdens());
		scn.FreepsDismissRevealedCards();
		// A [dwarven] possession, borne or in the support area, or a [dwarven] follower of either race; not a
		// [dwarven] artifact or ally, nor a possession or follower of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(axe, chariot, bifur, roac));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arkenstone, grimir, ironforged, thrush));
		// A play from a draw deck is 0 to 1 (Q-0019).
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(axe);

		// Balin is the only [dwarven] character, so the Axe goes on him unasked.
		assertAttachedTo(axe, balin);
		assertInZone(Zone.DECK, chariot, bifur, roac, arkenstone, grimir, ironforged, thrush);
		assertInDiscard(service);
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(1, scn.GetBurdens());
		// As the Axe left the deck looked through, and the shuffle after the play; the Shadow deck not at all.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlaysADwarvenFollowerFromTheDrawDeckToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var service = scn.GetFreepsCard("service");
		var chariot = scn.GetFreepsCard("chariot");
		var bifur = scn.GetFreepsCard("bifur");
		var roac = scn.GetFreepsCard("roac");
		var bilbo = scn.GetRingBearer();
		scn.MoveCardsToHand(service);

		scn.StartGame();

		assertInZone(Zone.DECK, chariot, bifur, roac);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetBurdens());
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(service);
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(1, scn.GetBurdens());
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(chariot, bifur, roac));
		scn.FreepsChooseCardBPFromSelection(bifur);

		assertInZone(Zone.SUPPORT, bifur);
		assertInZone(Zone.DECK, chariot, roac);
		assertInDiscard(service);
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotPlayableWhileBilboIsExhaustedThoughAnotherShireHobbitCompanionCouldExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var service = scn.GetFreepsCard("service");
		var bifur = scn.GetFreepsCard("bifur");
		var merry = scn.GetFreepsCard("merry");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(service);

		scn.StartGame();
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(bilbo, 3);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.DECK, bifur);
		assertTrue(scn.IsExhausted(bilbo));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(service));

		assertInZone(Zone.HAND, service);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetBurdens());
	}

	@Test
	public void IsNotPlayableWithNoDwarvenPossessionOrFollowerInTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var service = scn.GetFreepsCard("service");
		var axe = scn.GetFreepsCard("axe");
		var chariot = scn.GetFreepsCard("chariot");
		var bifur = scn.GetFreepsCard("bifur");
		var roac = scn.GetFreepsCard("roac");
		var arkenstone = scn.GetFreepsCard("arkenstone");
		var grimir = scn.GetFreepsCard("grimir");
		var ironforged = scn.GetFreepsCard("ironforged");
		var thrush = scn.GetFreepsCard("thrush");
		var balin = scn.GetFreepsCard("balin");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToHand(service);
		scn.MoveCardsToDiscard(axe, chariot, bifur, roac);

		scn.StartGame();

		assertInDiscard(axe, chariot, bifur, roac);
		assertInZone(Zone.DECK, arkenstone, grimir, ironforged, thrush);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(service));

		assertInZone(Zone.HAND, service);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetBurdens());
	}
}
