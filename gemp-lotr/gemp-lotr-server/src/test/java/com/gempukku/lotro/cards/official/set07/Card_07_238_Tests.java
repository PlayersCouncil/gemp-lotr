package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_238_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("knights", "7_238");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, Valiant and nothing else
					put("guard2", "5_83");
					// Rider's Mount: [rohan] mount, "Bearer must be a [rohan] Man"; its text acts only in skirmishes
					put("mount", "4_287");
					put("mount2", "4_287");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion; a strength bonus only
					// Shadowfax: [gandalf] mount for Gandalf; its texts are about hand weapons and skirmishes
					put("shadowfax", "4_100");
					put("chain", "4_278");      // Heavy Chain: a [rohan] possession that is no mount, borne by a Man
					put("guma", "4_277");       // Guma, Plains Farmer: [rohan] Man ally, who may bear Rider's Mount
					// Cards for the Shadow hand, and one for the table so that there is a maneuver phase: none is played
					put("patrol", "1_177");     // Goblin Patrol Troop: no text
					put("troop", "1_177");
					put("runner", "1_178");     // Goblin Runner: its text is about being played
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void KnightsofHisHouseStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Knights of His House
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Choose an opponent. For each mounted companion you spot, that opponent must remove (1) or place
		 * a random card from hand beneath his or her draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("knights");

		assertEquals("Knights of His House", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void HeSpotsUpToEachMountedCompanionAndTheShadowPlayerChoosesForEachOneSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knights = scn.GetFreepsCard("knights");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		var gandalf = scn.GetFreepsCard("gandalf");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		var chain = scn.GetFreepsCard("chain");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(guard, guard2, gandalf);
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(guard, mount);
		scn.AttachCardsTo(gandalf, shadowfax);
		scn.AttachCardsTo(guard2, chain);
		scn.AttachCardsTo(guma, mount2);
		scn.MoveCardsToHand(knights);

		var patrol = scn.GetShadowCard("patrol");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertAttachedTo(mount, guard);
		assertAttachedTo(shadowfax, gandalf);
		assertAttachedTo(chain, guard2);
		assertAttachedTo(mount2, guma);
		scn.MoveMinionsToTable(patrol);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.MoveCardsToHand(troop, runner);
		scn.SetTwilight(3);
		scn.FreepsRefreshDecision();
		assertInHand(troop, runner);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(3, scn.GetTwilight());

		assertTrue(scn.FreepsPlayAvailable(knights));
		scn.FreepsPlayCard(knights);

		// Two mounted companions, a [rohan] Man and a [gandalf] Wizard: not the companion bearing a possession that
		// is no mount, nor the mounted ally.  He may spot fewer; the number comes filled in with all of them.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertEquals(2, (int) scn.FreepsGetChoiceDefault());
		scn.FreepsChoose("1");

		// One mounted companion spotted: one choice, the Shadow player's.
		assertEquals(List.of("Remove 1 twilight", "Place a random card from hand beneath draw deck"), scn.ShadowGetMultipleChoices());
		scn.ShadowChoose("Place a random card from hand beneath draw deck");

		var placed = scn.GetShadowBottomOfDeck();
		assertTrue(List.of(troop, runner).contains(placed));
		assertInZone(Zone.DECK, placed);
		assertInHand(placed == troop ? runner : troop);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(3, scn.GetTwilight());
		assertInDiscard(knights);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TwoSpottedAgainstNoTwilightPlaceBothCardsUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knights = scn.GetFreepsCard("knights");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var gandalf = scn.GetFreepsCard("gandalf");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		scn.MoveCompanionsToTable(guard, gandalf);
		scn.AttachCardsTo(guard, mount);
		scn.AttachCardsTo(gandalf, shadowfax);
		scn.MoveCardsToHand(knights);

		var patrol = scn.GetShadowCard("patrol");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertAttachedTo(mount, guard);
		assertAttachedTo(shadowfax, gandalf);
		scn.MoveMinionsToTable(patrol);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.MoveCardsToHand(troop, runner);
		scn.SetTwilight(0);
		scn.FreepsRefreshDecision();
		assertInHand(troop, runner);
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPlayCard(knights);
		scn.FreepsChoose("2");

		// No twilight to remove: both cards go beneath his draw deck, and he is asked nothing.
		assertEquals(0, scn.GetShadowHandCount());
		assertInZone(Zone.DECK, troop, runner);
		assertEquals(Set.of(troop, runner), Set.of(scn.GetFromBottomOfShadowDeck(1), scn.GetFromBottomOfShadowDeck(2)));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AgainstAnEmptyHandTheTwilightIsRemovedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knights = scn.GetFreepsCard("knights");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var gandalf = scn.GetFreepsCard("gandalf");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		scn.MoveCompanionsToTable(guard, gandalf);
		scn.AttachCardsTo(guard, mount);
		scn.AttachCardsTo(gandalf, shadowfax);
		scn.MoveCardsToHand(knights);

		var patrol = scn.GetShadowCard("patrol");

		scn.StartGame();
		assertAttachedTo(mount, guard);
		assertAttachedTo(shadowfax, gandalf);
		scn.MoveMinionsToTable(patrol);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(3);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(3, scn.GetTwilight());

		scn.FreepsPlayCard(knights);
		scn.FreepsChoose("2");

		// No card in hand to place: (1) is removed for each of the two, and he is asked nothing.
		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoMountedCompanionItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knights = scn.GetFreepsCard("knights");
		var guard2 = scn.GetFreepsCard("guard2");
		var chain = scn.GetFreepsCard("chain");
		var guma = scn.GetFreepsCard("guma");
		var mount2 = scn.GetFreepsCard("mount2");
		scn.MoveCompanionsToTable(guard2);
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(guard2, chain);
		scn.AttachCardsTo(guma, mount2);
		scn.MoveCardsToHand(knights);

		var patrol = scn.GetShadowCard("patrol");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertAttachedTo(chain, guard2);
		assertAttachedTo(mount2, guma);
		scn.MoveMinionsToTable(patrol);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.MoveCardsToHand(troop);
		scn.SetTwilight(3);
		scn.FreepsRefreshDecision();
		assertInHand(troop);
		assertEquals(3, scn.GetTwilight());

		// The companion bearing a possession that is no mount, and the mounted ally: nobody to spot.
		assertTrue(scn.FreepsPlayAvailable(knights));
		scn.FreepsPlayCard(knights);

		assertInDiscard(knights);
		assertInHand(troop);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knights = scn.GetFreepsCard("knights");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, mount);
		scn.MoveCardsToHand(knights);

		var patrol = scn.GetShadowCard("patrol");

		scn.StartGame();
		assertAttachedTo(mount, guard);
		scn.MoveMinionsToTable(patrol);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertInHand(knights);
		assertFalse(scn.FreepsPlayAvailable(knights));
	}
}
