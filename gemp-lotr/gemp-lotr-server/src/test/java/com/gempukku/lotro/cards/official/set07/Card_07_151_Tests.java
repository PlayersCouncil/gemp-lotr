package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_151_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hosts", "7_151");
					put("explorer", "4_250");   // Southron Explorer: a [raider] Southron Man; a regroup ability used only on purpose
					// Easterling Axeman: a [raider] Easterling Man; strength +3 and fierce only at 2 burdens
					put("axeman", "4_224");
					// Ulaire Attea, Khamul of the East: a [raider] minion who is a Nazgul and no Man; his text needs 3 burdens
					put("khamul", "103_61");
					put("brigand", "4_10");     // Dunlending Brigand: a Man who is not [raider]; no text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man of the Free Peoples player's

					// Cards to hold in hand and discard, of several kinds: their texts do nothing from a hand.
					put("troop", "1_177");      // Goblin Patrol Troop: a minion
					put("spear", "1_182");      // Goblin Spear: a possession
					put("call", "10_47");       // Rallying Call: a condition
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion
					put("secret", "1_71");      // Durin's Secret: an event

					// Gandalf, Wise Guide: "Response: If an event is played, exert Gandalf twice to cancel that event."
					put("wiseguide", "19_8");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HostsStillUnfoughtStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Hosts Still Unfought
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: To play, spot 2 [raider] Men.<br>Discard any number of cards from hand. The Free Peoples player then discards any number of cards from hand. For each card you discarded more than the Free Peoples player, add (1).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hosts");

		assertEquals("Hosts Still Unfought", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWithOneRaiderManAndCardsThatAreNotBesideHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var hosts = scn.GetShadowCard("hosts");
		var explorer = scn.GetShadowCard("explorer");
		var axeman = scn.GetShadowCard("axeman");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(hosts);
		assertInZone(Zone.HAND, hosts);

		scn.StartGame();
		// One [raider] Man; beside him a [raider] minion who is no Man, a Man who is not [raider], and the Free
		// Peoples player's own Man.
		scn.MoveMinionsToTable(explorer, khamul, brigand);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(hosts));

		// A second [raider] Man, an Easterling.
		scn.MoveMinionsToTable(axeman);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(hosts));
	}

	@Test
	public void ThreeDiscardedAgainstOneAdds2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var fpspear = scn.GetFreepsCard("spear");
		var secret = scn.GetFreepsCard("secret");
		scn.MoveCardsToHand(gimli, fpspear, secret);

		var hosts = scn.GetShadowCard("hosts");
		var explorer = scn.GetShadowCard("explorer");
		var axeman = scn.GetShadowCard("axeman");
		var troop = scn.GetShadowCard("troop");
		var spear = scn.GetShadowCard("spear");
		var call = scn.GetShadowCard("call");
		scn.MoveCardsToHand(hosts, troop, spear, call);
		assertInZone(Zone.HAND, gimli, fpspear, secret, hosts, troop, spear, call);

		scn.StartGame();
		// Two [raider] Men of two titles, a Southron and an Easterling.
		scn.MoveMinionsToTable(explorer, axeman);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(hosts));
		scn.ShadowPlayCard(hosts);

		// Any number, from none: a minion, a possession and a condition are all offered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, spear, call));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(troop, spear, call);
		assertInDiscard(troop, spear, call);

		// Then the Free Peoples player, also from none: a companion, a possession and an event.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, fpspear, secret));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(secret);

		assertInDiscard(secret, hosts);
		assertInZone(Zone.HAND, gimli, fpspear);
		assertEquals(7, scn.GetTwilight()); // 5 + (3 - 1)
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void OneDiscardedAgainstNoneAdds1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(gimli);

		var hosts = scn.GetShadowCard("hosts");
		var explorer = scn.GetShadowCard("explorer");
		var axeman = scn.GetShadowCard("axeman");
		var troop = scn.GetShadowCard("troop");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToHand(hosts, troop, spear);
		assertInZone(Zone.HAND, gimli, hosts, troop, spear);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, axeman);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(5, scn.GetTwilight());
		scn.ShadowPlayCard(hosts);
		scn.ShadowChooseCards(spear);
		assertInDiscard(spear);
		assertInZone(Zone.HAND, troop);

		scn.FreepsDeclineChoosing();
		assertInZone(Zone.HAND, gimli);
		assertEquals(6, scn.GetTwilight()); // 5 + (1 - 0)
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void FewerDiscardedThanTheFreePeoplesPlayerAddsNothingAndRemovesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var secret = scn.GetFreepsCard("secret");
		scn.MoveCardsToHand(gimli, secret);

		var hosts = scn.GetShadowCard("hosts");
		var explorer = scn.GetShadowCard("explorer");
		var axeman = scn.GetShadowCard("axeman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(hosts, troop);
		assertInZone(Zone.HAND, gimli, secret, hosts, troop);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, axeman);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(5, scn.GetTwilight());
		scn.ShadowPlayCard(hosts);
		scn.ShadowChooseCards(troop);
		assertInDiscard(troop);

		scn.FreepsChooseCards(gimli, secret);
		assertInDiscard(gimli, secret);
		// 1 discarded against 2: nothing added, and nothing taken away.
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NoneDiscardedByEitherPlayerAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(gimli);

		var hosts = scn.GetShadowCard("hosts");
		var explorer = scn.GetShadowCard("explorer");
		var axeman = scn.GetShadowCard("axeman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(hosts, troop);
		assertInZone(Zone.HAND, gimli, hosts, troop);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, axeman);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(5, scn.GetTwilight());
		scn.ShadowPlayCard(hosts);
		scn.ShadowDeclineChoosing();
		scn.FreepsDeclineChoosing();

		assertInZone(Zone.HAND, gimli, troop);
		assertInDiscard(hosts);
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CancelledByGandalfTheCardsDiscardedForItStayDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wiseguide = scn.GetFreepsCard("wiseguide");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(wiseguide);
		scn.MoveCardsToHand(gimli);

		var hosts = scn.GetShadowCard("hosts");
		var explorer = scn.GetShadowCard("explorer");
		var axeman = scn.GetShadowCard("axeman");
		var troop = scn.GetShadowCard("troop");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToHand(hosts, troop, spear);
		assertInZone(Zone.HAND, gimli, hosts, troop, spear);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, axeman);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(wiseguide));
		scn.ShadowPlayCard(hosts);
		// The discard is the price, chosen and paid before anything can answer the play.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, spear));
		scn.ShadowChooseCards(troop, spear);
		assertInDiscard(troop, spear);

		assertTrue(scn.FreepsActionAvailable(wiseguide));
		scn.FreepsUseCardAction(wiseguide);
		assertEquals(2, scn.GetWoundsOn(wiseguide));

		// Cancelled: the Free Peoples player discards nothing, nothing is added, and the cards paid stay paid.
		assertInDiscard(hosts, troop, spear);
		assertInZone(Zone.HAND, gimli);
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
