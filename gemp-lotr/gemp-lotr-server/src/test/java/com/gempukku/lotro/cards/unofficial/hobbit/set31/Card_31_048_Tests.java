package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_048_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer, the one Bilbo in play.
	// Site 6 is Esgaroth 30_54, site 7 Erebor 30_55 and site 8 Smaug's Den 30_56; none of their abilities is offered
	// on these boards.  Turn 1's phases are at The Troll Hoard 30_50 (site 2).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("jewel", "31_48");

					put("thorin", "30_19");     // Thorin, Oakenshield: vitality 4; +1 strength for each [dwarven] follower, and he bears none
					put("balin", "30_2");       // Balin, Brother of Dwalin: a companion who is no Thorin, strength 7; a Fellowship ability not used

					put("bats", "32_30");       // Dark Bats: strength 4, vitality 2, so that there are maneuver phases; its Shadow ability not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void TheArkenstoneStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: The Arkenstone, King's Jewel
		 * Unique: True
		 * Side: Shadow
		 * Culture: Smaug
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype:
		 * Strength: -2
		 * Game Text: Plays on Bilbo.
		 * 	At sites 7 to 9, wound bearer at the end of the maneuver phase. (If bearer is Bilbo, add a doubt instead.)
		 * 	Thorin gains this ability: "Maneuver: Add a doubt to transfer the Arkenstone to Thorin (or add 3 doubts to discard it)."
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("jewel");

		assertEquals("The Arkenstone", card.getBlueprint().getTitle());
		assertEquals("King's Jewel", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SMAUG, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(-2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnBilbo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jewel = scn.GetShadowCard("jewel");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin, balin);
		scn.MoveCardsToHand(jewel);

		scn.StartGame();
		assertInHand(jewel);
		scn.FreepsPass();
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(jewel));
		scn.ShadowPlayCard(jewel);

		// Bilbo is the one Bilbo in play: it goes on him unasked, and not on Thorin or Balin.
		assertAttachedTo(jewel, bilbo);
		assertEquals(0, scn.GetAttachedCards(thorin).size());
		assertEquals(0, scn.GetAttachedCards(balin).size());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite7ItsBearerIsWoundedAtTheEndOfEachManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jewel = scn.GetShadowCard("jewel");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable(thorin, balin);
		// On Thorin, where his own ability transfers it.
		scn.AttachCardsTo(thorin, jewel);

		scn.StartGame();
		assertAttachedTo(jewel, thorin);
		scn.SkipToSite(6);
		scn.FreepsPass();
		scn.MoveMinionsToTable(bats);
		scn.ShadowRefreshDecision();
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetBurdens());

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Nothing at the start of the maneuver phase; the bearer, and only he, is wounded as it ends.
		scn.PassManeuverActions();
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		// Nothing more at the end of the archery phase.
		scn.SkipToAssignments();
		assertEquals(1, scn.GetWoundsOn(thorin));
		scn.FreepsAssignToMinions(balin, bats);
		scn.FreepsResolveSkirmish(balin);
		scn.PassSkirmishActions();

		// A second move in the same turn, to site 8: a second maneuver phase, a second wound.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(8, scn.GetCurrentSiteNumber());
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(1, scn.GetWoundsOn(thorin));
		scn.PassManeuverActions();
		assertEquals(2, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void AnExhaustedBearerIsWoundedAndKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jewel = scn.GetShadowCard("jewel");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable(thorin, balin);
		scn.AttachCardsTo(thorin, jewel);

		scn.StartGame();
		assertAttachedTo(jewel, thorin);
		scn.SkipToSite(6);
		scn.FreepsPass();
		scn.MoveMinionsToTable(bats);
		scn.ShadowRefreshDecision();
		assertEquals(7, scn.GetCurrentSiteNumber());

		scn.SkipToPhase(Phase.MANEUVER);
		// Vitality 4 with 3 wounds: exhausted, so a wound kills him (an exertion would not be made).
		scn.AddWoundsToChar(thorin, 3);
		assertEquals(3, scn.GetWoundsOn(thorin));
		scn.PassManeuverActions();

		assertInZone(Zone.DEAD, thorin);
		assertInDiscard(jewel);
		assertEquals(0, scn.GetWoundsOn(balin));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void AtSite9TheLastSiteItsBearerIsWoundedAtTheEndOfTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jewel = scn.GetShadowCard("jewel");
		var thorin = scn.GetFreepsCard("thorin");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(thorin, jewel);

		scn.StartGame();
		assertAttachedTo(jewel, thorin);
		scn.SkipToSite(8);
		scn.FreepsPass();
		scn.MoveMinionsToTable(bats);
		scn.ShadowRefreshDecision();
		assertEquals(9, scn.GetCurrentSiteNumber());

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(thorin));
		scn.PassManeuverActions();

		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void AtSite6ItsBearerIsNotWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jewel = scn.GetShadowCard("jewel");
		var thorin = scn.GetFreepsCard("thorin");
		var bats = scn.GetShadowCard("bats");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(thorin, jewel);

		scn.StartGame();
		assertAttachedTo(jewel, thorin);
		scn.SkipToSite(5);
		scn.FreepsPass();
		scn.MoveMinionsToTable(bats);
		scn.ShadowRefreshDecision();
		assertEquals(6, scn.GetCurrentSiteNumber());

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetBurdens());
		scn.PassManeuverActions();

		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetBurdens());
		assertAttachedTo(jewel, thorin);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void AtSite7WithBilboItsBearerADoubtIsAddedInsteadOfTheWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jewel = scn.GetShadowCard("jewel");
		var thorin = scn.GetFreepsCard("thorin");
		var bats = scn.GetShadowCard("bats");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(bilbo, jewel);

		scn.StartGame();
		assertAttachedTo(jewel, bilbo);
		scn.SkipToSite(6);
		scn.FreepsPass();
		scn.MoveMinionsToTable(bats);
		scn.ShadowRefreshDecision();
		assertEquals(7, scn.GetCurrentSiteNumber());

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetBurdens());
		scn.PassManeuverActions();

		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertAttachedTo(jewel, bilbo);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void ThorinAddsADoubtToTransferItFromBilboToHimself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jewel = scn.GetShadowCard("jewel");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var bats = scn.GetShadowCard("bats");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin, balin);
		scn.AttachCardsTo(bilbo, jewel);

		scn.StartGame();
		scn.MoveMinionsToTable(bats);
		assertAttachedTo(jewel, bilbo);
		assertEquals(0, scn.GetBurdens());

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		// Thorin has both abilities, the one cost and the other payable; Balin has neither.
		assertTrue(scn.FreepsActionAvailable(thorin, "transfer"));
		assertTrue(scn.FreepsActionAvailable(thorin, "discard The Arkenstone"));
		assertFalse(scn.FreepsActionAvailable(balin, "transfer"));
		assertFalse(scn.FreepsActionAvailable(balin, "discard The Arkenstone"));

		scn.FreepsUseCardAction(thorin, "transfer");
		assertEquals(1, scn.GetBurdens());
		assertAttachedTo(jewel, thorin);
		assertEquals(0, scn.GetAttachedCards(bilbo).size());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// With it on Thorin already, the transfer is still offered and paid for, and moves nothing.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(thorin, "transfer"));
		scn.FreepsUseCardAction(thorin, "transfer");
		assertEquals(2, scn.GetBurdens());
		assertAttachedTo(jewel, thorin);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ThorinAddsThreeDoubtsToDiscardIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jewel = scn.GetShadowCard("jewel");
		var thorin = scn.GetFreepsCard("thorin");
		var bats = scn.GetShadowCard("bats");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(bilbo, jewel);

		scn.StartGame();
		scn.MoveMinionsToTable(bats);
		assertAttachedTo(jewel, bilbo);
		assertEquals(0, scn.GetBurdens());

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable(thorin, "discard The Arkenstone"));
		scn.FreepsUseCardAction(thorin, "discard The Arkenstone");

		assertEquals(3, scn.GetBurdens());
		assertInDiscard(jewel);
		assertEquals(0, scn.GetAttachedCards(bilbo).size());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ThorinsAbilitiesAreNotOfferedInTheFellowshipPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jewel = scn.GetShadowCard("jewel");
		var thorin = scn.GetFreepsCard("thorin");
		var bats = scn.GetShadowCard("bats");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(bilbo, jewel);

		scn.StartGame();
		assertAttachedTo(jewel, bilbo);

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(thorin, "transfer"));
		assertFalse(scn.FreepsActionAvailable(thorin, "discard The Arkenstone"));

		// The same board in the maneuver phase: offered.
		scn.MoveMinionsToTable(bats);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(thorin, "transfer"));
		assertTrue(scn.FreepsActionAvailable(thorin, "discard The Arkenstone"));
	}
}
