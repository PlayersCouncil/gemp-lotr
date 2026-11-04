package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_082_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trample", "6_82");

					put("soldier", "4_254");    // Southron Soldier: [raider] Southron Man, no text but ambush
					put("mumak", "5_73");       // Mumak: [raider] mount on a Southron (bearer fierce)
					put("desert", "4_220");     // Desert Soldier: [raider] Southron Man; his trigger is at the start of a skirmish
					put("warmumak", "5_78");    // War Mumak: [raider] mount on a Southron (bearer fierce)
					put("bow", "4_247");        // Southron Bow: a [raider] possession that is no mount, on a [raider] Man
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] minion who is no Man (a Nazgul)
					put("steed", "1_208");      // Black Steed: a mount on a Nazgul
					put("scout", "15_77");      // Easterling Scout: a [men] Man
					put("beast", "17_40");      // Beast of War: a mount on a [men] Man

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: an ally, no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TrampleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Trample
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Spot a mounted [raider] Man to discard a companion (except the Ring-bearer). The Free Peoples player may add (4) to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("trample");

		assertEquals("Trample", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithAMountedRaiderManItDiscardsACompanionWhoIsNotTheRingBearerWhenThePreventionIsDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trample = scn.GetShadowCard("trample");
		var soldier = scn.GetShadowCard("soldier");
		var mumak = scn.GetShadowCard("mumak");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToHand(trample);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.AttachCardsTo(soldier, mumak);
		assertAttachedTo(mumak, soldier);
		assertInHand(trample);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(trample));
		scn.ShadowPlayCard(trample);

		// Companions of three cultures and races, Ring-bound and unbound; not the Ring-bearer, an ally or a minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, celeborn, soldier));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);

		assertTrue(scn.FreepsDecisionAvailable("Would you like to add (4)"));
		scn.FreepsChooseNo();

		assertInDiscard(aragorn, trample);
		assertInPlay(gimli, sam, celeborn, frodo);
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayAdd4ToPreventTheDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trample = scn.GetShadowCard("trample");
		var soldier = scn.GetShadowCard("soldier");
		var mumak = scn.GetShadowCard("mumak");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(trample);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.AttachCardsTo(soldier, mumak);
		assertAttachedTo(mumak, soldier);
		assertInHand(trample);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		int pool = scn.GetTwilight();
		scn.ShadowPlayCard(trample);
		scn.ShadowChooseCard(gimli);
		assertTrue(scn.FreepsDecisionAvailable("Would you like to add (4)"));
		scn.FreepsChooseYes();

		assertInPlay(gimli, aragorn);
		assertEquals(pool + 4, scn.GetTwilight());
		assertInDiscard(trample);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NotPlayableWithoutAMountedRaiderMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trample = scn.GetShadowCard("trample");
		var soldier = scn.GetShadowCard("soldier");
		var mumak = scn.GetShadowCard("mumak");
		var bow = scn.GetShadowCard("bow");
		var khamul = scn.GetShadowCard("khamul");
		var steed = scn.GetShadowCard("steed");
		var scout = scn.GetShadowCard("scout");
		var beast = scn.GetShadowCard("beast");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(trample);

		scn.StartGame();
		// A [raider] Man bearing a possession that is no mount, a mounted [raider] minion who is no Man, and a
		// mounted Man of another culture.
		scn.MoveMinionsToTable(soldier, khamul, scout);
		scn.AttachCardsTo(soldier, bow);
		scn.AttachCardsTo(khamul, steed);
		scn.AttachCardsTo(scout, beast);
		assertAttachedTo(bow, soldier);
		assertAttachedTo(steed, khamul);
		assertAttachedTo(beast, scout);
		assertInHand(trample);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(trample));

		// The [raider] Man mounted: playable.
		scn.AttachCardsTo(soldier, mumak);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(trample));
	}

	@Test
	public void WithNoCompanionButTheRingBearerItIsPlayedForNothingAndThePreventionIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trample = scn.GetShadowCard("trample");
		var desert = scn.GetShadowCard("desert");
		var warmumak = scn.GetShadowCard("warmumak");
		var frodo = scn.GetRingBearer();
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToHand(trample);

		scn.StartGame();
		scn.MoveMinionsToTable(desert);
		scn.AttachCardsTo(desert, warmumak);
		assertAttachedTo(warmumak, desert);
		assertInHand(trample);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(trample));
		scn.ShadowPlayCard(trample);

		assertFalse(scn.FreepsDecisionAvailable("Would you like to add (4)"));
		assertInDiscard(trample);
		assertInPlay(frodo, celeborn);
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
