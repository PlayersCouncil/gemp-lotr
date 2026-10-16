package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_084_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hunting", "3_84");

					put("lemenya", "1_232");    // Úlairë Lemenya: [wraith] Nazgûl; weakens only bearers of a [wraith] condition
					put("khamul", "103_61");    // Úlairë Attëa, Khamûl of the East: a [raider] Nazgûl; his bonuses need 3 burdens
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc and no Nazgûl; its bonus needs 2 wounds on the Ring-bearer
					put("misadventure", "1_129"); // The Misadventure of Mr. Underhill: "The twilight cost of each companion played to a site which is not a sanctuary is +2"

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a maneuver ability used only on purpose
					put("aragornsbow", "1_90"); // Aragorn's Bow: ranged weapon, bearer must be Aragorn; an archery ability
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion, vitality 3; stronger only against a Nazgûl
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion, vitality 3; an archery ability
					put("orophin", "1_56");     // Orophin, Lórien Bowman: Elf ally, vitality 3; an archery ability
					put("bow1", "1_41");        // Elven Bow: ranged weapon, bearer must be an Elf; makes bearer an archer
					put("bow2", "1_41");
					put("bow3", "1_41");
					put("gimli", "1_13");       // Gimli, Son of Glóin: Dwarf companion, cost 2, vitality 3; a skirmish ability
					put("axe", "1_9");          // Dwarven Axe: a hand weapon, bearer must be a Dwarf

					// Cards for the Free Peoples hand, by twilight cost
					put("wreathed", "1_238");   // Wreathed in Shadow: cost 0
					put("runner", "1_178");     // Goblin Runner: cost 1
					put("wallcrawler", "1_184"); // Goblin Wallcrawler: cost 2
					put("servant", "3_58");     // Isengard Servant: cost 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheyWillNeverStopHuntingYouStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: They Will Never Stop Hunting You
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Spot a Nazgûl to reveal a card at random from the Free Peoples player's hand. Exert a companion bearing a ranged weapon X times, where X is the twilight cost of the card revealed.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hunting");

		assertEquals("They Will Never Stop Hunting You", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWithoutANazgul() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunting = scn.GetShadowCard("hunting");
		var hound = scn.GetShadowCard("hound");
		var runner = scn.GetFreepsCard("runner");
		scn.MoveCardsToHand(hunting, runner);
		assertInHand(hunting, runner);

		scn.StartGame();
		// A [wraith] minion who is no Nazgûl.
		scn.MoveMinionsToTable(hound);
		scn.SetTwilight(5);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.GetTwilight() >= 2);
		assertFalse(scn.ShadowPlayAvailable(hunting));
	}

	@Test
	public void RevealsACardToTheShadowPlayerWhoExertsACompanionBearingARangedWeaponThatManyTimes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunting = scn.GetShadowCard("hunting");
		var lemenya = scn.GetShadowCard("lemenya");
		var shadowRunner = scn.GetShadowCard("runner");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var orophin = scn.GetFreepsCard("orophin");
		var wallcrawler = scn.GetFreepsCard("wallcrawler");
		scn.MoveCompanionsToTable(aragorn, arwen, legolas, gimli);
		scn.MoveAlliesToTable(orophin);
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("aragornsbow"));
		scn.AttachCardsTo(arwen, scn.GetFreepsCard("bow1"));
		scn.AttachCardsTo(legolas, scn.GetFreepsCard("bow2"));
		scn.AttachCardsTo(orophin, scn.GetFreepsCard("bow3"));
		scn.AttachCardsTo(gimli, scn.GetFreepsCard("axe"));
		scn.MoveCardsToHand(hunting, shadowRunner, wallcrawler);
		assertInHand(hunting, shadowRunner, wallcrawler);
		assertAttachedTo(scn.GetFreepsCard("aragornsbow"), aragorn);
		assertAttachedTo(scn.GetFreepsCard("bow1"), arwen);
		assertAttachedTo(scn.GetFreepsCard("bow2"), legolas);
		assertAttachedTo(scn.GetFreepsCard("bow3"), orophin);
		assertAttachedTo(scn.GetFreepsCard("axe"), gimli);

		scn.StartGame();
		// Legolas, vitality 3 with 2 wounds, is exhausted.
		scn.AddWoundsToChar(legolas, 2);
		scn.MoveMinionsToTable(lemenya);
		assertEquals(2, scn.GetWoundsOn(legolas));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowPlayAvailable(hunting));
		scn.ShadowPlayCard(hunting);

		// The Free Peoples player's one card is shown to the Shadow player, his own hand is not, and the hand's
		// owner is shown nothing.
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(wallcrawler, false));
		assertFalse(scn.ShadowHasCardChoiceAvailable(shadowRunner, false));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		scn.ShadowDismissRevealedCards();

		// Companions of either culture bearing a ranged weapon; not the exhausted one, the one bearing a hand
		// weapon, the ally bearing a bow nor the Ring-bearer, who bears no weapon.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, arwen));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(legolas, gimli, orophin, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		scn.ShadowChooseCard(aragorn);

		// Goblin Wallcrawler costs 2: Aragorn exerts twice.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInHand(wallcrawler);
		assertInDiscard(hunting);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void SpottingANazgulWhoIsNotWraithACardOfCostOneExertsOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunting = scn.GetShadowCard("hunting");
		var khamul = scn.GetShadowCard("khamul");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var runner = scn.GetFreepsCard("runner");
		scn.MoveCompanionsToTable(aragorn, arwen);
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("aragornsbow"));
		scn.AttachCardsTo(arwen, scn.GetFreepsCard("bow1"));
		scn.MoveCardsToHand(hunting, runner);
		assertInHand(hunting, runner);
		assertAttachedTo(scn.GetFreepsCard("bow1"), arwen);

		scn.StartGame();
		// The [raider] Nazgûl is the only Nazgûl.
		scn.MoveMinionsToTable(khamul);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(hunting));
		scn.ShadowPlayCard(hunting);
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, false));
		scn.ShadowDismissRevealedCards();

		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, arwen));
		assertEquals(0, scn.GetWoundsOn(arwen));
		scn.ShadowChooseCard(arwen);

		// Goblin Runner costs 1: one exertion.
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ACardOfCostZeroExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunting = scn.GetShadowCard("hunting");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var wreathed = scn.GetFreepsCard("wreathed");
		scn.MoveCompanionsToTable(aragorn, arwen);
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("aragornsbow"));
		scn.AttachCardsTo(arwen, scn.GetFreepsCard("bow1"));
		scn.MoveCardsToHand(hunting, wreathed);
		assertInHand(hunting, wreathed);
		assertAttachedTo(scn.GetFreepsCard("bow1"), arwen);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		scn.ShadowPlayCard(hunting);
		assertTrue(scn.ShadowHasCardChoiceAvailable(wreathed, false));
		scn.ShadowDismissRevealedCards();

		// Wreathed in Shadow costs 0.  The game still asks for the companion, and the one chosen exerts no times.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, arwen));
		scn.ShadowChooseCard(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertInDiscard(hunting);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheExertionsAreTheCostOfTheCardShownAndNotOfAnotherCardOfTheHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunting = scn.GetShadowCard("hunting");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetFreepsCard("runner");
		var wallcrawler = scn.GetFreepsCard("wallcrawler");
		var servant = scn.GetFreepsCard("servant");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("aragornsbow"));
		scn.MoveCardsToHand(hunting, runner, wallcrawler, servant);
		assertInHand(hunting, runner, wallcrawler, servant);
		assertAttachedTo(scn.GetFreepsCard("aragornsbow"), aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.ShadowPlayCard(hunting);
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		// Which card is shown is chance; what is read is which it was.
		var runnerShown = scn.ShadowHasCardChoiceAvailable(runner, false);
		var wallcrawlerShown = scn.ShadowHasCardChoiceAvailable(wallcrawler, false);
		var servantShown = scn.ShadowHasCardChoiceAvailable(servant, false);
		assertEquals(1, (runnerShown ? 1 : 0) + (wallcrawlerShown ? 1 : 0) + (servantShown ? 1 : 0));
		scn.ShadowDismissRevealedCards();

		// Costs 1, 2 and 3: Aragorn, the only companion bearing a ranged weapon, exerts as often as the card shown costs.
		var shownCost = runnerShown ? 1 : (wallcrawlerShown ? 2 : 3);
		assertEquals(shownCost, scn.GetWoundsOn(aragorn));
		assertInHand(runner, wallcrawler, servant);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithAnEmptyHandNothingIsRevealedAndNobodyExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunting = scn.GetShadowCard("hunting");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("aragornsbow"));
		scn.MoveCardsToHand(hunting);
		assertInHand(hunting);
		assertAttachedTo(scn.GetFreepsCard("aragornsbow"), aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// An event may be played though it will do nothing.
		assertTrue(scn.ShadowPlayAvailable(hunting));
		scn.MarkGameLog();
		scn.ShadowPlayCard(hunting);

		// Nobody is shown anything and the log says why; X is 0, so Aragorn, the one companion bearing a ranged
		// weapon, exerts no times.
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.GameLogSinceMarkSays("no cards in " + scn.P1() + "'s hand"));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInDiscard(hunting);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void XIsTheCostPrintedOnTheCardThoughACardInPlayMakesItDearerToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunting = scn.GetShadowCard("hunting");
		var lemenya = scn.GetShadowCard("lemenya");
		var misadventure = scn.GetShadowCard("misadventure");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("aragornsbow"));
		scn.MoveCardsToHand(hunting, gimli);
		scn.MoveCardsToSupportArea(misadventure);
		assertInHand(hunting, gimli);
		assertInZone(Zone.SUPPORT, misadventure);
		assertAttachedTo(scn.GetFreepsCard("aragornsbow"), aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// At Ettenmoors, no sanctuary, a companion costs 2 more to play: Gimli, printed 2, reads 4 in hand.
		assertEquals(4, scn.GetTwilightCost(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.ShadowPlayCard(hunting);
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, false));
		scn.ShadowDismissRevealedCards();

		// X is 2: a card in play changes another card's cost only as that card is played.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoCompanionBearingARangedWeaponTheCardIsStillRevealedAndNobodyExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunting = scn.GetShadowCard("hunting");
		var lemenya = scn.GetShadowCard("lemenya");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var orophin = scn.GetFreepsCard("orophin");
		var wallcrawler = scn.GetFreepsCard("wallcrawler");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(orophin);
		scn.AttachCardsTo(gimli, scn.GetFreepsCard("axe"));
		scn.AttachCardsTo(orophin, scn.GetFreepsCard("bow3"));
		scn.MoveCardsToHand(hunting, wallcrawler);
		assertInHand(hunting, wallcrawler);
		assertAttachedTo(scn.GetFreepsCard("axe"), gimli);
		assertAttachedTo(scn.GetFreepsCard("bow3"), orophin);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// The only ranged weapon is borne by an ally, and the companion's weapon is a hand weapon.
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowPlayAvailable(hunting));
		scn.ShadowPlayCard(hunting);
		assertTrue(scn.ShadowHasCardChoiceAvailable(wallcrawler, false));
		scn.ShadowDismissRevealedCards();

		// The card cost 2, and nobody is asked to exert.
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(hunting);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
