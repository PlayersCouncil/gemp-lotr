package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_309_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rope", "7_309");
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7, vitality 2; his text answers a weapon played on him
					put("pillager", "7_275");   // Gorgoroth Pillager: [sauron] Orc of another title, strength 10; his text works only while he is stacked
					put("sentry", "10_84");     // Cirith Ungol Sentry: a [sauron] minion who is no Orc (Uruk-hai); damage +1 only
					put("hound", "7_192");      // Morgul Hound: an Orc who is not [sauron] ([wraith]), strength 5; +4 and fierce only at 2 wounds on the Ring-bearer

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: companion, strength 7; a skirmish ability used only on purpose
					// Gandalf, Wise Guide: "Response: If an event is played, exert Gandalf twice to cancel that event."
					put("wiseguide", "19_8");

					// Cards to hold in hand and discard, of several kinds: their texts do nothing from a hand.
					put("troop", "1_177");      // Goblin Patrol Troop: a minion
					put("spear", "1_182");      // Goblin Spear: a possession
					put("call", "10_47");       // Rallying Call: a condition
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion
					put("secret", "1_71");      // Durin's Secret: an event
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RopeandWinchStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Rope and Winch
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: To play, spot 2 [sauron] Orcs.
		 * Discard any number of cards from hand. The Free Peoples player then discards any number of cards from hand. For each card you discarded more than the Free Peoples player, make a [sauron] Orc strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rope");

		assertEquals("Rope and Winch", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedInASkirmishUntilASecondSauronOrcIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var rope = scn.GetShadowCard("rope");
		var chieftain = scn.GetShadowCard("chieftain");
		var pillager = scn.GetShadowCard("pillager");
		var sentry = scn.GetShadowCard("sentry");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(rope);
		assertInHand(rope);

		scn.StartGame();
		// One [sauron] Orc, beside a [sauron] minion who is no Orc and an Orc who is not [sauron].
		scn.MoveMinionsToTable(chieftain, sentry, hound);
		scn.SetTwilight(10);
		assertEquals(10, scn.GetTwilight());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, chieftain);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(rope));

		// A second [sauron] Orc, of another title.
		scn.MoveMinionsToTable(pillager);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(rope));
	}

	@Test
	public void ThreeDiscardedAgainstOneMakesASauronOrcStrengthPlus2TwiceOverTheSameOrcChosenTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var fpspear = scn.GetFreepsCard("spear");
		var secret = scn.GetFreepsCard("secret");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(gimli, fpspear, secret);

		var rope = scn.GetShadowCard("rope");
		var chieftain = scn.GetShadowCard("chieftain");
		var pillager = scn.GetShadowCard("pillager");
		var sentry = scn.GetShadowCard("sentry");
		var hound = scn.GetShadowCard("hound");
		var troop = scn.GetShadowCard("troop");
		var spear = scn.GetShadowCard("spear");
		var call = scn.GetShadowCard("call");
		// A Free Peoples card in the Shadow player's own hand: the discard is of any card, of either side.
		var shadowgimli = scn.GetShadowCard("gimli");
		scn.MoveCardsToHand(rope, troop, spear, call, shadowgimli);
		assertInZone(Zone.HAND, gimli, fpspear, secret, rope, troop, spear, call, shadowgimli);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, pillager, sentry, hound);
		scn.SetTwilight(10);
		assertEquals(10, scn.GetTwilight());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, chieftain}, new PhysicalCardImpl[]{boromir, pillager});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(10, scn.GetStrength(pillager));
		assertTrue(scn.ShadowPlayAvailable(rope));
		scn.ShadowPlayCard(rope);

		// Any number, from none: a minion, a possession, a condition and a Free Peoples companion are all offered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, spear, call, shadowgimli));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(troop, spear, call);
		assertInDiscard(troop, spear, call);
		assertInZone(Zone.HAND, shadowgimli);

		// Then the Free Peoples player, also from none: a companion, a possession and an event.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, fpspear, secret));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(secret);
		assertInDiscard(secret);
		assertInZone(Zone.HAND, gimli, fpspear);

		// 3 - 1: two bonuses, each a choice of its own.  Either [sauron] Orc, the one in this skirmish or the one
		// whose skirmish is to come; not the Uruk-hai or the [wraith] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, pillager));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentry, hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(chieftain);
		assertEquals(9, scn.GetStrength(chieftain));

		// The second may go to the same Orc (CR 4.2 "for each").
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, pillager));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentry, hound));
		scn.ShadowChooseCard(chieftain);

		assertEquals(11, scn.GetStrength(chieftain)); // 7 + 2 + 2
		assertEquals(10, scn.GetStrength(pillager));
		assertEquals(8, scn.GetStrength(sentry));
		assertEquals(5, scn.GetStrength(hound));
		assertInDiscard(rope);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void OneDiscardedAgainstNoneMakesAnOrcWhoseSkirmishIsToComeStrongerForThisSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(gimli);

		var rope = scn.GetShadowCard("rope");
		var chieftain = scn.GetShadowCard("chieftain");
		var pillager = scn.GetShadowCard("pillager");
		var troop = scn.GetShadowCard("troop");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToHand(rope, troop, spear);
		assertInZone(Zone.HAND, gimli, rope, troop, spear);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, pillager);
		scn.SetTwilight(10);
		assertEquals(10, scn.GetTwilight());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, chieftain}, new PhysicalCardImpl[]{boromir, pillager});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(10, scn.GetStrength(pillager));
		scn.ShadowPlayCard(rope);
		scn.ShadowChooseCards(spear);
		assertInDiscard(spear);
		assertInZone(Zone.HAND, troop);

		scn.FreepsDeclineChoosing();
		assertInZone(Zone.HAND, gimli);

		// 1 - 0: one bonus, given to the Pillager, assigned to Boromir and not in this skirmish.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, pillager));
		scn.ShadowChooseCard(pillager);

		assertEquals(12, scn.GetStrength(pillager));
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn's skirmish was the phase the bonus lasted for: the Pillager is 10 in his own.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, pillager));
		assertEquals(10, scn.GetStrength(pillager));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AsManyDiscardedAsTheFreePeoplesPlayerMakesNoOrcStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(gimli);

		var rope = scn.GetShadowCard("rope");
		var chieftain = scn.GetShadowCard("chieftain");
		var pillager = scn.GetShadowCard("pillager");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToHand(rope, spear);
		assertInZone(Zone.HAND, gimli, rope, spear);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, pillager);
		scn.SetTwilight(10);
		assertEquals(10, scn.GetTwilight());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, chieftain}, new PhysicalCardImpl[]{boromir, pillager});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(10, scn.GetStrength(pillager));
		scn.ShadowPlayCard(rope);
		scn.ShadowChooseCards(spear);
		scn.FreepsChooseCards(gimli);
		assertInDiscard(spear, gimli, rope);

		// 1 against 1: no Orc is chosen and none is stronger.
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(10, scn.GetStrength(pillager));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CancelledByGandalfTheCardsDiscardedForItStayDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var wiseguide = scn.GetFreepsCard("wiseguide");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, wiseguide);
		scn.MoveCardsToHand(gimli);

		var rope = scn.GetShadowCard("rope");
		var chieftain = scn.GetShadowCard("chieftain");
		var pillager = scn.GetShadowCard("pillager");
		var troop = scn.GetShadowCard("troop");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToHand(rope, troop, spear);
		assertInZone(Zone.HAND, gimli, rope, troop, spear);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, pillager);
		scn.SetTwilight(10);
		assertEquals(10, scn.GetTwilight());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, chieftain}, new PhysicalCardImpl[]{wiseguide, pillager});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(wiseguide));
		assertEquals(7, scn.GetStrength(chieftain));
		scn.ShadowPlayCard(rope);
		// The discard is the price, chosen and paid before anything can answer the play.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, spear));
		scn.ShadowChooseCards(troop, spear);
		assertInDiscard(troop, spear);

		assertTrue(scn.FreepsActionAvailable(wiseguide));
		scn.FreepsUseCardAction(wiseguide);
		assertEquals(2, scn.GetWoundsOn(wiseguide));

		// Cancelled: the Free Peoples player discards nothing, no Orc is stronger, and the cards paid stay paid.
		assertInDiscard(rope, troop, spear);
		assertInZone(Zone.HAND, gimli);
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(10, scn.GetStrength(pillager));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
