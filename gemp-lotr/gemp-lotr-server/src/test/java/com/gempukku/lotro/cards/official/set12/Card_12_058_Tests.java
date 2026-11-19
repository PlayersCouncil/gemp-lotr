package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_058_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("companies", "12_58");
					put("companies2", "12_58");

					// Ceremonial Armor: [men] possession in the support area, cost 2; its texts answer a [men]
					// minion discarded in the maneuver phase and a wound to a [men] minion
					put("armor", "17_41");
					// Hides: [dunland] possession in the support area, cost 1; its response answers a wound to a
					// [dunland] Man
					put("hides", "4_19");
					put("trident", "12_77");    // War Trident: [men] hand weapon, cost 1, on a [men] minion; strength bonuses in a skirmish
					put("dagger", "18_69");     // Henchman's Dagger: [men] hand weapon, cost 0, on a [men] minion; no other text
					put("poleaxe", "11_95");    // Poleaxe: [men] hand weapon, cost 1, on a [men] minion; a skirmish ability
					put("elder", "11_77");      // Elder of Dunland: [men] Man; damage +1 at a plains site
					put("corps", "11_73");      // Corps of Harad: [men] Man; fierce only at a battleground
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc with no text

					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: the Gandalf Roll of Thunder spots
					// Roll of Thunder: [gandalf] Fellowship or Maneuver event. "Spot Gandalf to discard a Shadow
					// possession or Shadow artifact."
					put("roll1", "4_99");
					put("roll2", "4_99");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an Elf companion; stronger only against a Nazgul
					// Cast It Into the Fire!: [elven] Maneuver event. "Spot an Elf to make each opponent discard a
					// card from his or her hand. Draw a card for each card discarded in this way."
					put("castit", "3_11");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetResponseScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("companies", "12_58");
					put("corps", "11_73");      // Corps of Harad: [men] Man; fierce only at a battleground
					// Henchman's Dagger: [men] possession, cost 0, on a [men] minion; a hand weapon that may be
					// borne beside another
					put("dagger", "18_69");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a ranger, vitality 4
					put("vengeance", "1_106");  // Gondor's Vengeance: Regroup event. Exert a ranger companion to discard a minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A [men] possession that costs something stacked on Countless Companies, and a [men] minion to bear it.
	protected VirtualTableScenario GetStackedBowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("companies", "12_58");
					put("corps", "11_73");      // Corps of Harad: [men] Man; fierce only at a battleground
					// Long Battle Bow: [men] possession, cost 2, on a [men] minion; when played, may make the Free
					// Peoples player exert his or her Man, and there is none
					put("bow", "11_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CountlessCompaniesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Countless Companies
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Response:</b> If a [men] possession is discarded from play, stack it here.<br><b>Shadow:</b> Remove (1) to play a possession stacked here as if from hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("companies");

		assertEquals("Countless Companies", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	// Ketura on Q-0034: an optional action with no cost whose whole effect would do nothing is not offered.  Once
	// the discarded possession is stacked here it is no longer in the discard pile, and a second use of the
	// Response for the same discard would stack nothing.  The game used to offer it again each time it was taken,
	// for as long as the player went on taking it (F-0357).
	@Test
	public void OnceTheDiscardedPossessionIsStackedHereTheResponseIsNotOfferedForItAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetResponseScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var vengeance = scn.GetFreepsCard("vengeance");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(vengeance);

		var companies = scn.GetShadowCard("companies");
		var corps = scn.GetShadowCard("corps");
		var dagger = scn.GetShadowCard("dagger");
		scn.MoveCardsToSupportArea(companies);

		scn.StartGame();
		scn.MoveMinionsToTable(corps);
		scn.AttachCardsTo(corps, dagger);
		// Nobody is assigned to the Corps, so it is in play when the regroup phase starts.
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertAttachedTo(dagger, corps);
		assertEquals(0, scn.GetStackedCards(companies).size());

		// The Corps is discarded, and the dagger it bore is discarded from play with it
		scn.FreepsPlayCard(vengeance);
		assertInZone(Zone.DISCARD, corps, dagger);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(companies));

		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.STACKED, dagger);
		assertEquals(1, scn.GetStackedCards(companies).size());
		assertTrue(scn.GetStackedCards(companies).contains(dagger));

		// Nothing is left in the discard pile for the Response to stack, and it is the Shadow player's turn to
		// act in the regroup phase
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(companies));
		assertInZone(Zone.DISCARD, corps);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	// "Remove (1) to play a possession stacked here as if from hand": a play as an effect is treated as a cost,
	// so the pool must pay for the removal and the possession together (F-0046, F-0141).  The definition did not
	// count the (1) it removes: with exactly the possession's cost in the pool the ability was offered, took the
	// token and played nothing.
	// F-0452.
	@Test
	public void ThePoolMustPayForTheRemovalAndTheStackedPossessionTogether() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStackedBowScenario();

		var companies = scn.GetShadowCard("companies");
		var corps = scn.GetShadowCard("corps");
		var bow = scn.GetShadowCard("bow");
		scn.MoveCardsToSupportArea(companies);
		scn.StackCardsOn(companies, bow);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(corps);

		// (2) in the pool: enough for the Bow or for the removal, not for both.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(companies));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(companies));
		scn.ShadowUseCardAction(companies);

		assertEquals(0, scn.GetTwilight());
		assertAttachedTo(bow, corps);
		assertEquals(0, scn.GetStackedCards(companies).size());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EitherCopyMayStackADiscardedMenPossessionAndNeitherAnswersAPossessionOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var roll1 = scn.GetFreepsCard("roll1");
		var roll2 = scn.GetFreepsCard("roll2");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(roll1, roll2);

		var companies = scn.GetShadowCard("companies");
		var companies2 = scn.GetShadowCard("companies2");
		var armor = scn.GetShadowCard("armor");
		var hides = scn.GetShadowCard("hides");
		scn.MoveCardsToSupportArea(companies, companies2, armor, hides);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, armor, hides);
		assertEquals(0, scn.GetStackedCards(companies).size());
		assertEquals(0, scn.GetStackedCards(companies2).size());

		// A [dunland] possession discarded from play: neither copy answers it.
		scn.FreepsPlayCard(roll1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, hides));
		scn.FreepsChooseCard(hides);
		assertInZone(Zone.DISCARD, hides);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A [men] possession in the support area discarded from play: each copy offers to stack it, and the one
		// taken holds it.
		scn.FreepsPlayCard(roll2);
		assertInZone(Zone.DISCARD, armor);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(companies));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(companies2));
		scn.ShadowChooseOptionalTrigger(companies2);

		assertInZone(Zone.STACKED, armor);
		assertTrue(scn.GetStackedCards(companies2).contains(armor));
		assertEquals(1, scn.GetStackedCards(companies2).size());
		assertEquals(0, scn.GetStackedCards(companies).size());
		assertInZone(Zone.DISCARD, hides);
		// With the Armor stacked, the other copy has nothing left to stack.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(companies));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedForAMenPossessionDiscardedFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var castit = scn.GetFreepsCard("castit");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(castit);

		var companies = scn.GetShadowCard("companies");
		var trident = scn.GetShadowCard("trident");
		scn.MoveCardsToSupportArea(companies);
		scn.MoveCardsToHand(trident);

		scn.StartGame();
		scn.MoveMinionsToTable("elder");
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.HAND, trident);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(0, scn.GetStackedCards(companies).size());

		// The Shadow player discards his one card, the War Trident, from hand: no discard from play.
		scn.FreepsPlayCard(castit);
		assertInZone(Zone.DISCARD, trident);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(companies));
		assertEquals(0, scn.GetStackedCards(companies).size());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ShadowAbilityPlaysAPossessionStackedOnThisCopyOnAMenMinionOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var companies = scn.GetShadowCard("companies");
		var companies2 = scn.GetShadowCard("companies2");
		var trident = scn.GetShadowCard("trident");
		var dagger = scn.GetShadowCard("dagger");
		var poleaxe = scn.GetShadowCard("poleaxe");
		var elder = scn.GetShadowCard("elder");
		var corps = scn.GetShadowCard("corps");
		scn.MoveCardsToSupportArea(companies, companies2);
		scn.StackCardsOn(companies, trident, dagger);
		scn.StackCardsOn(companies2, poleaxe);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(elder, corps);
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(5, scn.GetTwilight());
		assertEquals(2, scn.GetStackedCards(companies).size());
		assertEquals(1, scn.GetStackedCards(companies2).size());
		assertTrue(scn.ShadowActionAvailable(companies));

		scn.ShadowUseCardAction(companies);
		// The possessions stacked on this copy, and not the one on the other.
		assertTrue(scn.ShadowHasCardChoiceAvailable(trident, dagger));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(poleaxe));
		scn.ShadowChooseCard(trident);

		// Played as if from hand: on a [men] minion of the player's choice.
		assertTrue(scn.ShadowHasCardChoiceAvailable(elder, corps));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(corps);

		// 1 removed + 1 for the War Trident.
		assertEquals(3, scn.GetTwilight());
		assertAttachedTo(trident, corps);
		assertEquals(1, scn.GetStackedCards(companies).size());
		assertTrue(scn.GetStackedCards(companies).contains(dagger));
		assertTrue(scn.GetStackedCards(companies2).contains(poleaxe));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhenTheStackedPossessionHasNoBearerToGoOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var companies = scn.GetShadowCard("companies");
		var dagger = scn.GetShadowCard("dagger");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(companies);
		scn.StackCardsOn(companies, dagger);

		scn.StartGame();
		scn.FreepsPass();
		// A minion, but no [men] minion to bear the Dagger.
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.GetStackedCards(companies).contains(dagger));
		assertFalse(scn.ShadowActionAvailable(companies));
	}
}
