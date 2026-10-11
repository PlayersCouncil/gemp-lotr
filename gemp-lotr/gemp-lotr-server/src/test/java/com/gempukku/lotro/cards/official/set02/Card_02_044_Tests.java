package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_044_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("business", "2_44");
					put("uruk", "1_151");       // Uruk Savage: [isengard] Uruk-hai, cost 2, site 5; damage +1 and nothing else
					put("goblinman", "2_42");   // Goblin Man: [isengard] Orc; discarded underground; a Shadow ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: a [moria] minion; cheated in, so its text does nothing
					put("armory", "1_157");     // Uruk-hai Armory: an [isengard] condition in the support area; its text is about archery
					put("troop1", "1_177");     // Goblin Patrol Troop: a minion with no text, to fill a hand
					put("troop2", "1_177");
					put("keyward", "12_105");   // Troll's Keyward: [orc] Orc, vitality 3. "Shadow: Exert Troll's Keyward and reveal your hand to add (1) for each [orc] Troll revealed."

					put("mirror", "1_55");      // The Mirror of Galadriel: "Maneuver: If an opponent has at least 7 cards in hand, exert Galadriel to look at 2 of those cards at random. Discard one and replace the other."
					put("curse1", "1_36");      // Curse Their Foul Feet!: "Fellowship: Exert an Elf to reveal an opponent's hand. That player discards a card from hand for each Orc revealed."
					put("curse2", "1_36");
					put("galadriel", "6_18");   // Galadriel, Keeper of Nenya: Elf ally, vitality 3; heals herself at the start of each turn; a skirmish ability
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, vitality 3; her text is about skirmishing a Nazgul
					put("mallorn", "1_54");     // Mallorn-trees: "Fellowship: Reveal an [elven] card from hand and place it beneath your draw deck."
					put("storm1", "3_6");       // Storm of Argument: [dwarven] Fellowship event, cost 1. "Reveal a card at random from an opponent's hand. Shuffle up to X [dwarven] cards from your discard pile into your draw deck, where X is the twilight cost of the card revealed."
					put("storm2", "3_6");
				}},
				// Site 6 is Galadriel's Glade: "Sanctuary. Fellowship: Exert an Elf to look at an opponent's hand."
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NoBusinessofOursStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: No Business of Ours
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot an [isengard] minion. Plays to your support area.
		 * 		The Free Peoples player may not look at or reveal cards in any Shadow player's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("business");

		assertEquals("No Business of Ours", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void NoBusinessSpotsAnIsengardMinionToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		var uruk = scn.GetShadowCard("uruk");
		var runner = scn.GetShadowCard("runner");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToHand(business, uruk);

		scn.StartGame();
		// A [moria] minion and an [isengard] condition: neither is an [isengard] minion.
		scn.MoveMinionsToTable(runner);
		scn.MoveCardsToSupportArea(armory);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.HAND, business);
		assertFalse(scn.ShadowPlayAvailable(business));

		scn.ShadowPlayCard(uruk);
		assertTrue(scn.ShadowPlayAvailable(business));

		// It costs nothing.
		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(business);
		assertEquals(twilight, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, business);
		assertNotAttachedTo(business, uruk);
		assertNotAttachedTo(business, scn.GetRingBearer());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnIsengardOrcMayBeSpottedToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		var goblinman = scn.GetShadowCard("goblinman");
		scn.MoveCardsToHand(business);

		scn.StartGame();
		scn.MoveMinionsToTable(goblinman);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(business));

		scn.ShadowPlayCard(business);
		assertInZone(Zone.SUPPORT, business);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FreepsCannotRevealCardsInShadowHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		var uruk = scn.GetShadowCard("uruk");
		var armory = scn.GetShadowCard("armory");
		// The card under test waits in the discard pile and is put into play from there below.
		scn.MoveCardsToDiscard(business);
		// No Orc in the Shadow player's hand, so the curse makes nobody discard.
		scn.MoveCardsToHand(uruk, armory);

		var galadriel = scn.GetFreepsCard("galadriel");
		var curse1 = scn.GetFreepsCard("curse1");
		var curse2 = scn.GetFreepsCard("curse2");
		scn.MoveCardsToHand(curse1, curse2);
		scn.MoveAlliesToTable(galadriel);

		scn.StartGame();

		// Without the card, the curse reveals the hand.
		scn.FreepsPlayCard(curse1);
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertTrue(scn.FreepsDecisionAvailable("Hand"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(uruk, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(armory, false));
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveCardsToSupportArea(business); //slide in No Business between actions
		assertInZone(Zone.SUPPORT, business);

		// With it, the curse is still played and its cost paid, and nothing is shown.
		assertTrue(scn.FreepsPlayAvailable(curse2));
		scn.FreepsPlayCard(curse2);
		assertEquals(2, scn.GetWoundsOn(galadriel));
		assertInDiscard(curse2);
		assertFalse(scn.FreepsDecisionAvailable("Hand"));
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FreepsCannotLookAtCardsInShadowHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		var uruk = scn.GetShadowCard("uruk");
		// The card under test waits in the discard pile, where the Mirror's random look and discard cannot reach
		// it; it is put into play from there below.
		scn.MoveCardsToDiscard(business);
		// Eight cards in hand, so that after one is discarded the Mirror's 7 are still there.
		scn.MoveCardsToHand(scn.GetShadowCard("goblinman"), scn.GetShadowCard("runner"), scn.GetShadowCard("armory"),
				scn.GetShadowCard("troop1"), scn.GetShadowCard("troop2"), scn.GetShadowCard("mirror"),
				scn.GetShadowCard("curse1"), scn.GetShadowCard("curse2"));

		var galadriel = scn.GetFreepsCard("galadriel");
		var mirror = scn.GetFreepsCard("mirror");
		scn.MoveCardsToSupportArea(mirror);
		scn.MoveAlliesToTable(galadriel);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(8, scn.GetShadowHandCount());
		assertInDiscard(business);

		// Without the card, the Mirror looks at 2 cards and discards one.
		scn.FreepsUseCardAction(mirror);
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertTrue(scn.FreepsDecisionAvailable("Opponent's hand"));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseAny();
		assertEquals(7, scn.GetShadowHandCount());

		scn.ShadowPass();
		scn.MoveCardsToSupportArea(business); //slide in No Business between actions
		assertInZone(Zone.SUPPORT, business);

		// With it, the Mirror is still used and its cost paid, and nothing is looked at or discarded.
		assertTrue(scn.FreepsActionAvailable(mirror));
		scn.FreepsUseCardAction(mirror);
		assertEquals(2, scn.GetWoundsOn(galadriel));
		assertFalse(scn.FreepsDecisionAvailable("Opponent's hand"));
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(7, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void FreepsCannotRevealACardAtRandomFromTheShadowHandEither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		var uruk = scn.GetShadowCard("uruk");
		// The card under test waits in the discard pile and is put into play from there below.
		scn.MoveCardsToDiscard(business);
		// One card in the Shadow hand, so that the card revealed at random is not left to chance.
		scn.MoveCardsToHand(uruk);

		var storm1 = scn.GetFreepsCard("storm1");
		var storm2 = scn.GetFreepsCard("storm2");
		// No [dwarven] card in the Free Peoples discard pile for the event's second sentence to shuffle in, until
		// the first event is itself played.
		scn.MoveCardsToHand(storm1, storm2);

		scn.StartGame();

		// Without the card, the event shows the one card.
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(storm1));
		scn.FreepsPlayCard(storm1);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.FreepsHasCardChoiceAvailable(uruk, false));
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		assertInDiscard(storm1);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveCardsToSupportArea(business);
		assertInZone(Zone.SUPPORT, business);

		// With it, the event is still played and paid for, and nothing is shown.
		assertTrue(scn.FreepsPlayAvailable(storm2));
		scn.FreepsPlayCard(storm2);
		assertEquals(2, scn.GetTwilight());
		assertFalse(scn.FreepsDecisionAvailable("Hand"));
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertInDiscard(storm2);
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FreepsCannotLookAtTheShadowHandByASitesTextEither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		var uruk = scn.GetShadowCard("uruk");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToDiscard(business);

		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SetShadowHand(uruk, armory);

		var glade = scn.GetCurrentSite();
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsActionAvailable(glade));

		// Without the card, the site shows the whole hand. Arwen is the only Elf, so she pays.
		scn.FreepsUseCardAction(glade);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsHasCardChoiceAvailable(uruk, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(armory, false));
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveCardsToSupportArea(business);
		assertInZone(Zone.SUPPORT, business);

		// With it, the Elf exerts and nothing is shown.
		assertTrue(scn.FreepsActionAvailable(glade));
		scn.FreepsUseCardAction(glade);
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayStillRevealHisOwnHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		var keyward = scn.GetShadowCard("keyward");
		var uruk = scn.GetShadowCard("uruk");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToSupportArea(business);
		scn.MoveCardsToHand(uruk, armory);

		scn.StartGame();
		scn.MoveMinionsToTable(keyward);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SUPPORT, business);
		assertEquals(0, scn.GetWoundsOn(keyward));
		assertTrue(scn.ShadowActionAvailable(keyward));
		int twilight = scn.GetTwilight();

		// The restriction is on the Free Peoples player: a hand its own owner reveals is shown to him.
		scn.ShadowUseCardAction(keyward);
		assertEquals(1, scn.GetWoundsOn(keyward));
		assertTrue(scn.FreepsHasCardChoiceAvailable(uruk, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(armory, false));
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		// No [orc] Troll among them, so nothing is added.
		assertEquals(twilight, scn.GetTwilight());
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayStillRevealCardsFromHisOwnHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		scn.MoveCardsToSupportArea(business);

		var mallorn = scn.GetFreepsCard("mallorn");
		var curse1 = scn.GetFreepsCard("curse1");
		scn.MoveCardsToSupportArea(mallorn);
		scn.MoveCardsToHand(curse1);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, business);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(mallorn));

		// The restriction is on a Shadow player's hand: the one [elven] card in his own is shown to the Shadow player.
		scn.FreepsUseCardAction(mallorn);
		assertTrue(scn.ShadowHasCardChoiceAvailable(curse1, false));
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();

		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.DECK, curse1);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
