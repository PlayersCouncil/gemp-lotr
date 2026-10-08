package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_090_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hand", "53_90");

					// The minions.
					put("warden", "1_259");     // Morgul Warden: unique [sauron] Orc, vitality 3
					put("mouth", "12_118");     // The Mouth of Sauron: unique [sauron] Man, vitality 3; an assignment ability
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, not unique; text only when a weapon is played on him
					put("lurtz", "1_127");      // Lurtz: unique [isengard] Uruk-hai; a maneuver ability used only on purpose

					// The Free Peoples side.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4
					put("bow", "1_90");         // Aragorn's Bow: possession
					put("pipe", "1_91");        // Aragorn's Pipe: possession
					put("anduril", "7_79");     // Anduril, Flame of the West: an artifact, cheated on so its play trigger never fires
					put("poison", "8_28");      // Spider Poison: a Shadow possession; nothing unless its bearer is assigned
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer
					put("gimli", "1_13");       // Gimli, Son of Gloin: vitality 3
					put("armor", "1_8");        // Dwarven Armor: possession, nothing outside a skirmish
					put("legolas", "1_50");     // Legolas, Greenleaf: vitality 3
					put("sword", "1_299");      // Hobbit Sword: possession, no text
					put("sword2", "1_299");
					put("bilbo", "1_284");      // Bilbo: Hobbit ally with a fellowship ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HandofSauronStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Hand of Sauron
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Exert a unique [sauron] minion to exert a companion (except the Ring-bearer) and discard a
		 * possession attached to that companion.  The Free Peoples player may discard 1 card at random from their
		 * hand to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hand");

		assertEquals("Hand of Sauron", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAUniqueSauronMinionToExertACompanionAndDiscardAPossessionHeBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hand = scn.GetShadowCard("hand");
		var warden = scn.GetShadowCard("warden");
		var chieftain = scn.GetShadowCard("chieftain");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		var pipe = scn.GetFreepsCard("pipe");
		var anduril = scn.GetFreepsCard("anduril");

		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril, bow, pipe);
		scn.MoveCardsToHand(hand);
		scn.MoveCardsToHand(scn.GetFreepsCard("chieftain"), scn.GetFreepsCard("lurtz"));

		scn.StartGame();
		scn.MoveMinionsToTable(warden, chieftain);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(warden));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.ShadowPlayAvailable(hand));

		// Morgul Warden is the one unique [sauron] minion, and Aragorn the one companion who is not the
		// Ring-bearer: both are chosen without asking.
		scn.ShadowPlayCard(hand);
		assertEquals(1, scn.GetWoundsOn(warden));
		assertEquals(0, scn.GetWoundsOn(chieftain));

		// Two possessions; Anduril is an artifact.
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(bow, pipe));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(anduril));
		scn.ShadowChooseCard(bow);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInDiscard(bow);
		assertAttachedTo(pipe, aragorn);
		assertAttachedTo(anduril, aragorn);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertInDiscard(hand);
		// The event costs nothing.
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void FreePeoplesPlayerMayDiscardACardAtRandomFromHandToPreventIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hand = scn.GetShadowCard("hand");
		var warden = scn.GetShadowCard("warden");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		var pipe = scn.GetFreepsCard("pipe");
		var held1 = scn.GetFreepsCard("chieftain");
		var held2 = scn.GetFreepsCard("lurtz");

		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, bow, pipe);
		scn.MoveCardsToHand(hand);
		scn.MoveCardsToHand(held1, held2);

		scn.StartGame();
		scn.MoveMinionsToTable(warden);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(warden));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.ShadowPlayCard(hand);
		scn.ShadowChooseCard(bow);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		// One of the two cards, at random and not of his choosing, is discarded; the other stays in hand.
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		// Nothing happens to Aragorn, but the Shadow player's cost stays paid.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertAttachedTo(bow, aragorn);
		assertAttachedTo(pipe, aragorn);
		assertEquals(1, scn.GetWoundsOn(warden));
		assertInDiscard(hand);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheMinionCompanionAndPossessionOfferedAreExactlyThoseTheTextNames() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hand = scn.GetShadowCard("hand");
		var warden = scn.GetShadowCard("warden");
		var mouth = scn.GetShadowCard("mouth");
		var chieftain = scn.GetShadowCard("chieftain");
		var lurtz = scn.GetShadowCard("lurtz");
		var poison = scn.GetShadowCard("poison");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		var pipe = scn.GetFreepsCard("pipe");
		var anduril = scn.GetFreepsCard("anduril");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var sword = scn.GetFreepsCard("sword");
		var sword2 = scn.GetFreepsCard("sword2");
		var bilbo = scn.GetFreepsCard("bilbo");

		scn.MoveCompanionsToTable(aragorn, sam, gimli);
		scn.MoveAlliesToTable(bilbo);
		scn.AttachCardsTo(aragorn, anduril, bow, pipe, poison);
		scn.AttachCardsTo(frodo, sword);
		scn.AttachCardsTo(bilbo, sword2);
		scn.AttachCardsTo(gimli, armor);
		scn.MoveCardsToHand(hand);
		scn.MoveCardsToHand(scn.GetFreepsCard("chieftain"));

		scn.StartGame();
		scn.MoveMinionsToTable(warden, mouth, chieftain, lurtz);
		// Gimli, vitality 3, exhausted: he cannot be exerted but bears a possession.
		scn.AddWoundsToChar(gimli, 2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowPlayCard(hand);
		// A unique [sauron] minion of either race; not one that is not unique, not a unique minion of another culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(warden, mouth));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(chieftain, lurtz));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(mouth);
		assertEquals(1, scn.GetWoundsOn(mouth));
		assertEquals(0, scn.GetWoundsOn(warden));

		// Any companion but the Ring-bearer, Ring-bound Sam included; not an ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, sam, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, bilbo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);

		// Any possession on Aragorn, the Shadow player's own included; not his artifact, not a possession
		// on anyone else.
		assertTrue(scn.ShadowHasCardChoiceAvailable(bow, pipe, poison));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(anduril, armor, sword, sword2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(poison);

		scn.FreepsChooseNo();

		assertInDiscard(poison);
		assertAttachedTo(bow, aragorn);
		assertAttachedTo(pipe, aragorn);
		assertAttachedTo(anduril, aragorn);
		assertAttachedTo(sword, frodo);
		assertAttachedTo(sword2, bilbo);
		assertAttachedTo(armor, gimli);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ACompanionBearingNoPossessionIsStillExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hand = scn.GetShadowCard("hand");
		var warden = scn.GetShadowCard("warden");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		var sam = scn.GetFreepsCard("sam");

		scn.MoveCompanionsToTable(aragorn, sam);
		scn.AttachCardsTo(aragorn, bow);
		scn.MoveCardsToHand(hand);
		scn.MoveCardsToHand(scn.GetFreepsCard("chieftain"));

		scn.StartGame();
		scn.MoveMinionsToTable(warden);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(sam));

		scn.ShadowPlayCard(hand);
		scn.ShadowChooseCard(sam);

		// The exertion is still to happen, so the Free Peoples player is asked.
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		assertEquals(1, scn.GetWoundsOn(sam));
		assertAttachedTo(bow, aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AnExhaustedCompanionLosesHisPossessionWithoutBeingExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hand = scn.GetShadowCard("hand");
		var warden = scn.GetShadowCard("warden");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");

		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(gimli, armor);
		scn.MoveCardsToHand(hand);
		scn.MoveCardsToHand(scn.GetFreepsCard("chieftain"));

		scn.StartGame();
		scn.MoveMinionsToTable(warden);
		scn.AddWoundsToChar(gimli, 2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.IsExhausted(gimli));

		scn.ShadowPlayCard(hand);
		scn.ShadowChooseCard(gimli);

		// The discard is still to happen, so the Free Peoples player is asked.
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		assertInDiscard(armor);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AnExhaustedCompanionBearingNoPossessionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hand = scn.GetShadowCard("hand");
		var warden = scn.GetShadowCard("warden");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		var sam = scn.GetFreepsCard("sam");
		var legolas = scn.GetFreepsCard("legolas");

		scn.MoveCompanionsToTable(aragorn, sam, legolas);
		scn.AttachCardsTo(aragorn, bow);
		scn.MoveCardsToHand(hand);
		scn.MoveCardsToHand(scn.GetFreepsCard("chieftain"));

		scn.StartGame();
		scn.MoveMinionsToTable(warden);
		// Legolas, vitality 3, exhausted and bearing nothing.
		scn.AddWoundsToChar(legolas, 2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowPlayCard(hand);
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(legolas));
		scn.ShadowChooseCard(aragorn);
		scn.FreepsChooseNo();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInDiscard(bow);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithOnlyTheRingBearerNothingHappensAndThePreventionIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hand = scn.GetShadowCard("hand");
		var warden = scn.GetShadowCard("warden");
		var frodo = scn.GetRingBearer();
		var sword = scn.GetFreepsCard("sword");

		scn.AttachCardsTo(frodo, sword);
		scn.MoveCardsToHand(hand);
		scn.MoveCardsToHand(scn.GetFreepsCard("chieftain"), scn.GetFreepsCard("lurtz"));

		scn.StartGame();
		scn.MoveMinionsToTable(warden);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(hand));
		scn.ShadowPlayCard(hand);

		// The cost is paid and the event is spent; there is no companion it can reach.
		assertEquals(1, scn.GetWoundsOn(warden));
		assertInDiscard(hand);
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertAttachedTo(sword, frodo);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoCardInHandTheFreePeoplesPlayerIsNotAskedToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hand = scn.GetShadowCard("hand");
		var warden = scn.GetShadowCard("warden");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");

		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, bow);
		scn.MoveCardsToHand(hand);

		scn.StartGame();
		scn.MoveMinionsToTable(warden);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetFreepsHandCount());

		scn.ShadowPlayCard(hand);

		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInDiscard(bow);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hand = scn.GetShadowCard("hand");
		var warden = scn.GetShadowCard("warden");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pipe = scn.GetFreepsCard("pipe");

		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, pipe);
		scn.MoveCardsToHand(hand);

		scn.StartGame();
		scn.MoveMinionsToTable(warden);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(hand));
		scn.ShadowPass();
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(hand));
	}

	@Test
	public void CannotBePlayedWhenTheOnlyUniqueSauronMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hand = scn.GetShadowCard("hand");
		var warden = scn.GetShadowCard("warden");
		var chieftain = scn.GetShadowCard("chieftain");
		var lurtz = scn.GetShadowCard("lurtz");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pipe = scn.GetFreepsCard("pipe");

		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, pipe);
		scn.MoveCardsToHand(hand);

		scn.StartGame();
		// A [sauron] minion that is not unique and a unique minion of another culture are fresh.
		scn.MoveMinionsToTable(warden, chieftain, lurtz);
		scn.AddWoundsToChar(warden, 2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.IsExhausted(warden));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(hand));
	}
}
