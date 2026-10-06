package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import com.gempukku.lotro.logic.modifiers.IsAdditionalRaceModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_004_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("horsemen", "102_4");

					// [dunland] minions that are not unique. All are Men of site 3, so they roam at site 2.
					put("brigand", "4_10");     // Dunlending Brigand: cost 2, strength 8, no text
					put("brigand2", "4_10");
					put("savage", "4_17");      // Dunlending Savage: cost 4, no text
					put("warrior", "4_18");     // Dunlending Warrior: cost 3; an assignment ability that needs an ally
					put("robber", "4_16");      // Dunlending Robber: cost 3; a trigger on winning a skirmish
					// Minions that nearly qualify
					put("chief", "4_20");       // Hill Chief: a unique [dunland] Man, cost 4; a Shadow ability that needs a controlled site
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion that is not unique, cost 6, site 4, no text

					// The two [dunland] possessions a minion can bear: "Bearer must be a [dunland] Man."
					put("axe", "4_26");         // Iron Axe: cost 1, strength +3, nothing else
					put("club", "4_36");        // War Club: cost 0, strength +2; a trigger on its bearer winning a skirmish
					put("club2", "4_36");
					// Cards that nearly qualify
					put("hides", "4_19");       // Hides: a [dunland] possession that plays to the support area, not on a minion
					put("nodefense", "4_28");   // No Defense: a [dunland] condition
					put("sword", "1_160");      // Uruk-hai Sword: an [isengard] possession, cost 1; "Bearer must be an Uruk-hai."
					put("sword2", "1_160");
					put("enraged", "1_254");    // Mordor Enraged: a [sauron] condition, cost 0; "Plays on a [sauron] minion."; a Shadow ability that exerts its bearer
					put("enraged2", "1_254");

					// Cards for the Free Peoples player's hand: 4 or more there and initiative is his
					put("guard1", "1_7");       // Dwarf Guard
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("guard4", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HorsemenTookYourLandsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Horsemen Took Your Lands
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Play a non-unique [Dunland] minion from your discard pile. If you have initiative, you may play a [dunland] possession on that minion from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("horsemen");

		assertEquals("Horsemen Took Your Lands", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysANonUniqueDunlandMinionFromTheDiscardPileAndNoPossessionWithoutInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horsemen = scn.GetShadowCard("horsemen");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");
		var warrior = scn.GetShadowCard("warrior");
		var robber = scn.GetShadowCard("robber");
		var chief = scn.GetShadowCard("chief");
		var troop = scn.GetShadowCard("troop");
		var axe = scn.GetShadowCard("axe");
		var club = scn.GetShadowCard("club");
		scn.MoveCardsToHand(horsemen, warrior);
		scn.MoveCardsToDiscard(brigand, savage, chief, troop, axe, club);
		// The Robber is in the Shadow player's draw deck and in the other player's discard pile, and in neither
		// player's hand.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("robber"));
		// Four cards in the Free Peoples player's hand: initiative is his.
		scn.MoveCardsToFreepsHand("guard1", "guard2", "guard3", "guard4");

		scn.StartGame();
		scn.SetTwilight(9);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 9 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(12, scn.GetTwilight());
		assertEquals(4, scn.GetFreepsHandCount());
		assertFalse(scn.ShadowHasInitiative());
		assertInZone(Zone.HAND, horsemen, warrior);
		assertInZone(Zone.DISCARD, brigand, savage, chief, troop, axe, club);
		assertInZone(Zone.DECK, robber);
		assertTrue(scn.ShadowPlayAvailable(horsemen));

		scn.ShadowPlayCard(horsemen);

		// The event's own cost
		assertEquals(11, scn.GetTwilight());
		// Two [dunland] minions that are not unique, of different costs
		assertTrue(scn.ShadowHasCardChoiceAvailable(brigand, savage));
		// A unique [dunland] minion, a minion of another culture (8 with its roaming penalty, which the pool
		// could pay), a [dunland] possession, a [dunland] minion in hand, and one that is in the draw deck and
		// in the other player's discard pile
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(chief, troop, axe, warrior, robber));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(savage);

		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		// 4 for the Savage + 2 for roaming at site 2
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.DISCARD, horsemen, brigand, chief, troop);
		assertInZone(Zone.HAND, warrior);

		// Without initiative no possession is offered, though two are there and the pool could pay for either.
		assertInZone(Zone.DISCARD, axe, club);
		assertTrue(scn.GetAttachedCards(savage).isEmpty());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithInitiativeADunlandPossessionFromTheDiscardPileIsPlayedOnThatMinion() throws DecisionResultInvalidException, CardNotFoundException, InvalidCardDefinitionException {
		var scn = GetScenario();

		var horsemen = scn.GetShadowCard("horsemen");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");
		var axe = scn.GetShadowCard("axe");
		var club = scn.GetShadowCard("club");
		var club2 = scn.GetShadowCard("club2");
		var hides = scn.GetShadowCard("hides");
		var nodefense = scn.GetShadowCard("nodefense");
		var sword = scn.GetShadowCard("sword");
		var sword2 = scn.GetShadowCard("sword2");
		var enraged = scn.GetShadowCard("enraged");
		var enraged2 = scn.GetShadowCard("enraged2");
		scn.MoveCardsToHand(horsemen, club2, sword2, enraged2);
		scn.MoveCardsToDiscard(brigand, axe, club, hides, nodefense, sword, enraged);
		// Three cards in the Free Peoples player's hand: the most with which the Shadow has initiative.
		scn.MoveCardsToFreepsHand("guard1", "guard2", "guard3");
		// No possession of another culture can be borne by a [dunland] Man. Making every [dunland] minion an
		// Uruk-hai as well lets them bear the Uruk-hai Sword, so that only its culture keeps it out.
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, scn.GenerateShadowFilter("culture(dunland),minion"), null, Race.URUK_HAI));
		// No [dunland] card but a possession goes on a minion. Making every [dunland] minion a [sauron] minion as
		// well lets them bear Mordor Enraged, and making Mordor Enraged a [dunland] card as well leaves only its
		// being a condition to keep it out.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, scn.GenerateShadowFilter("culture(dunland),minion"), null, Culture.SAURON));
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, scn.GenerateShadowFilter("name(Mordor Enraged)"), null, Culture.DUNLAND));

		scn.StartGame();
		// A [dunland] Man already in play, who is not "that minion"
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 4 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(7, scn.GetTwilight());
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertInZone(Zone.DISCARD, brigand, axe, club, hides, nodefense, sword, enraged);
		// The Sword in hand can be played: a [dunland] Man can now bear one.
		assertTrue(scn.ShadowPlayAvailable(sword2));
		// So can the Mordor Enraged in hand.
		assertTrue(scn.ShadowPlayAvailable(enraged2));
		assertTrue(scn.ShadowPlayAvailable(horsemen));

		scn.ShadowPlayCard(horsemen);

		// The Brigand is the one minion the event can play, and is played unasked.
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		// 1 for the event + 2 for the Brigand + 2 for roaming at site 2
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(axe, club));
		// A [dunland] possession that does not go on a minion, a [dunland] condition, a possession of another
		// culture that the Brigand could bear, and a [dunland] condition that the Brigand could bear
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hides, nodefense, sword, enraged));
		scn.ShadowChooseCardBPFromSelection(axe);

		// On the minion the event played, not the [dunland] Man who was there before
		assertAttachedTo(axe, brigand);
		assertTrue(scn.GetAttachedCards(savage).isEmpty());
		// 1 for the Iron Axe
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.DISCARD, horsemen, club, hides, nodefense, sword, enraged);
		// The copy of the War Club in hand was no part of the choice.
		assertInZone(Zone.HAND, club2, sword2, enraged2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithInitiativeALonePossessionInTheDiscardPileIsOfferedAndAcceptedIsPlayedOnThatMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horsemen = scn.GetShadowCard("horsemen");
		var brigand = scn.GetShadowCard("brigand");
		var chief = scn.GetShadowCard("chief");
		var club = scn.GetShadowCard("club");
		var hides = scn.GetShadowCard("hides");
		scn.MoveCardsToHand(horsemen);
		scn.MoveCardsToDiscard(brigand, chief, club, hides);
		scn.MoveCardsToFreepsHand("guard1", "guard2", "guard3");

		scn.StartGame();
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 2 set + 1 for Frodo + 2 for the shadow number of site 2: exactly what the event and the Brigand cost
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.ShadowHasInitiative());
		assertInZone(Zone.DISCARD, brigand, chief, club, hides);
		assertTrue(scn.ShadowPlayAvailable(horsemen));

		scn.ShadowPlayCard(horsemen);

		// 1 for the event + 2 for the Brigand + 2 for roaming at site 2
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);

		// The War Club is the one possession the event could play, and "you may" still asks: it is offered, with
		// leave to take none, and has not been played yet.
		assertInZone(Zone.DISCARD, club);
		assertTrue(scn.ShadowHasCardChoiceAvailable(club));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hides, chief));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCardBPFromSelection(club);

		assertAttachedTo(club, brigand);
		// 0 for the War Club
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.DISCARD, horsemen, chief, hides);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ThePossessionMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horsemen = scn.GetShadowCard("horsemen");
		var brigand = scn.GetShadowCard("brigand");
		var axe = scn.GetShadowCard("axe");
		var club = scn.GetShadowCard("club");
		scn.MoveCardsToHand(horsemen);
		scn.MoveCardsToDiscard(brigand, axe, club);
		scn.MoveCardsToFreepsHand("guard1", "guard2", "guard3");

		scn.StartGame();
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.ShadowPlayAvailable(horsemen));

		scn.ShadowPlayCard(horsemen);

		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		// 4 set + 1 for Frodo + 2 for site 2, less 1 for the event and 2 + 2 roaming for the Brigand
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(axe, club));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();

		assertInZone(Zone.DISCARD, axe, club);
		assertTrue(scn.GetAttachedCards(brigand).isEmpty());
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoNonUniqueDunlandMinionInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horsemen = scn.GetShadowCard("horsemen");
		var warrior = scn.GetShadowCard("warrior");
		var chief = scn.GetShadowCard("chief");
		var troop = scn.GetShadowCard("troop");
		var axe = scn.GetShadowCard("axe");
		scn.MoveCardsToHand(horsemen, warrior);
		// A unique [dunland] minion, a minion of another culture that is not unique, and a [dunland] possession
		scn.MoveCardsToDiscard(chief, troop, axe);

		scn.StartGame();
		scn.SetTwilight(9);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 9 set + 1 for Frodo + 2 for the shadow number of site 2: enough for the event and any of the minions
		assertEquals(12, scn.GetTwilight());
		assertInZone(Zone.DISCARD, chief, troop, axe);
		// The [dunland] minion in hand can be played from there, and is no use to the event.
		assertTrue(scn.ShadowPlayAvailable(warrior));
		assertFalse(scn.ShadowPlayAvailable(horsemen));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenThePoolIsOneShortOfTheEventAndTheMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horsemen = scn.GetShadowCard("horsemen");
		var brigand = scn.GetShadowCard("brigand");
		var brigand2 = scn.GetShadowCard("brigand2");
		scn.MoveCardsToHand(horsemen, brigand2);
		scn.MoveCardsToDiscard(brigand);

		scn.StartGame();
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 1 set + 1 for Frodo + 2 for the shadow number of site 2. The event is 1 and the Brigand 2 + 2 roaming:
		// 5 in all.
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.DISCARD, brigand);
		// 4 pays for a Brigand played from hand, so it is the event's own cost that leaves the pool short.
		assertTrue(scn.ShadowPlayAvailable(brigand2));
		assertFalse(scn.ShadowPlayAvailable(horsemen));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
