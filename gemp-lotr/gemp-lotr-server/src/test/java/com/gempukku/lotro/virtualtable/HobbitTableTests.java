package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Tests of the rig itself: a table in the Hobbit Draft Game format (HobbitDraft), which the table lays on that
 * format's own rules (engine pass 77, Ketura's item of 2026-11-04).  Its defaults are the Hobbit path (HobbitSites,
 * 30_49 to 30_57) and a Bilbo as Ring-bearer (BilboRB, 30_46); the Ring slot is left empty, as the format needs no
 * One Ring in a deck, and the HDG's One Ring, HobbitRing 30_48, is a [shire] artifact of the draw deck put on
 * Bilbo by legal play or by AttachCardsTo.  The rules are taken from the format and not from the sites: Hobbit
 * sites at a Multipath table are laid as any other path.  The minion is Goblin Runner 1_178 (strength 5,
 * vitality 1; its text is about being played).
 */
public class HobbitTableTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ring", VirtualTableScenario.HobbitRing);
					put("runner", "1_178");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void AHobbitDraftTableLaysTheHobbitPathABilboAndNoRingInTheRingSlot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		for (int number = 1; number <= 9; number++) {
			var site = scn.GetFreepsSite(number);
			assertEquals(VirtualTableScenario.HobbitSites.get("site" + number), site.getBlueprintId());
			assertEquals(SitesBlock.HOBBIT, site.getBlueprint().getSiteBlock());
			assertEquals(number, site.getBlueprint().getSiteNumber());
			assertEquals(VirtualTableScenario.HobbitSites.get("site" + number), scn.GetShadowSite(number).getBlueprintId());
		}

		var bilbo = scn.GetRingBearer();
		assertEquals(VirtualTableScenario.BilboRB, bilbo.getBlueprintId());
		assertEquals(VirtualTableScenario.BilboRB, scn.GetShadowRingBearer().getBlueprintId());
		// The Ring slot is empty for both players; the HDG's One Ring is in each draw deck under its alias.
		assertNull(scn.GetRing());
		assertNull(scn.GetShadowRing());
		var ring = scn.GetFreepsCard("ring");
		assertInZone(Zone.DECK, ring, scn.GetShadowCard("ring"));

		scn.StartGame();
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals("Bag End", scn.GetCurrentSite().getBlueprint().getTitle());
		assertInZone(Zone.FREE_CHARACTERS, bilbo);
		// Bilbo bears nothing: no Ring is put on him at set-up.
		assertTrue(scn.GetAttachedCards(bilbo).isEmpty());
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.RBWearingOneRing());
		assertEquals(3, scn.GetStrength(bilbo));
		assertEquals(4, scn.GetVitality(bilbo));
		assertEquals(8, scn.GetResistance(bilbo));
	}

	@Test
	public void TheHobbitRingIsPlayedFromHandOntoBilbo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var ring = scn.GetFreepsCard("ring");
		scn.MoveCardsToHand(ring);

		scn.StartGame();
		assertTrue(scn.FreepsPlayAvailable(ring));
		// Bilbo is the only bearer it can have, so he is chosen.
		scn.FreepsPlayCard(ring);

		assertAttachedTo(ring, bilbo);
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.RBWearingOneRing());
		// +1 vitality and +1 resistance, and no strength while it is not worn.
		assertEquals(3, scn.GetStrength(bilbo));
		assertEquals(5, scn.GetVitality(bilbo));
		assertEquals(9, scn.GetResistance(bilbo));
	}

	@Test
	public void TheHobbitRingCheatedOntoBilboIsWornByItsOwnSkirmishActionAndTakenOffAtRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var ring = scn.GetFreepsCard("ring");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AttachCardsTo(bilbo, ring);
		assertAttachedTo(ring, bilbo);
		assertEquals(5, scn.GetVitality(bilbo));
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, runner);
		scn.FreepsResolveSkirmish(bilbo);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(ring));
		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());
		assertEquals(5, scn.GetStrength(bilbo));

		// Bilbo, 5, ties the Runner, 5, and loses: the wound is a doubt (a burden) instead.
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(2, scn.GetBurdens());

		// The game's rule takes the Ring off at the start of the regroup phase, with no card in the Ring slot.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.RBWearingOneRing());
		assertEquals(3, scn.GetStrength(bilbo));
		assertAttachedTo(ring, bilbo);
	}

	@Test
	public void AHobbitDraftTableTakesTheRingBearerAndSitesItIsGiven() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ring", VirtualTableScenario.HobbitRing);
				}},
				VirtualTableScenario.FellowshipSites,
				"30_43",
				null,
				VirtualTableScenario.HobbitDraft
		);

		assertEquals("30_43", scn.GetRingBearer().getBlueprintId());
		assertEquals("1_319", scn.GetFreepsSite(1).getBlueprintId());
		assertNull(scn.GetRing());
	}

	@Test
	public void AHobbitDraftTableRefusesARingForTheRingSlot() {
		var refused = assertThrows(IllegalArgumentException.class, () -> new VirtualTableScenario(
				new HashMap<>(),
				null, null,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.HobbitDraft
		));
		assertTrue(refused.getMessage().contains("Ring slot"));

		assertThrows(IllegalArgumentException.class, () -> new VirtualTableScenario(
				new HashMap<>(),
				null, null,
				VirtualTableScenario.HobbitRing,
				VirtualTableScenario.HobbitDraft
		));
	}

	@Test
	public void TheHobbitRulesComeFromTheFormatAndNotFromTheSites() throws DecisionResultInvalidException, CardNotFoundException {
		// The Hobbit path at a Multipath table: the usual defaults, a Ring in the Ring slot on the Ring-bearer.
		var scn = new VirtualTableScenario(
				new HashMap<>(),
				VirtualTableScenario.HobbitSites,
				null, null
		);

		assertEquals("30_49", scn.GetFreepsSite(1).getBlueprintId());
		assertEquals(VirtualTableScenario.MDFrodo, scn.GetRingBearer().getBlueprintId());
		assertEquals(VirtualTableScenario.RulingRing, scn.GetRing().getBlueprintId());

		scn.StartGame();
		assertAttachedTo(scn.GetRing(), scn.GetRingBearer());
	}

	/**
	 * Esgaroth 30_54, site 6 of the Hobbit path, has an optional "At the start of the regroup phase, you may play a
	 * [esgaroth] or [dwarven] ally from your draw deck", offered whatever the deck holds, to whichever player moves
	 * the fellowship that turn.  SkipCurrentSite declines such an offer for either seat (G-0065, engine pass 78),
	 * so SkipToSite plays both turns at Esgaroth and on to Erebor 30_55, site 7, with no ally played.  Bard 30_22,
	 * an [esgaroth] ally, is in each deck for the offer to play.
	 */
	@Test
	public void SkipToSitePassesEsgarothsStartOfRegroupOfferOnBothTurns() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bard", "30_22");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
		var bard = scn.GetFreepsCard("bard");
		var shadowBard = scn.GetShadowCard("bard");

		scn.StartGame();
		scn.SkipToSite(7);

		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals("Erebor", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertNotInZone(Zone.SUPPORT, bard, shadowBard);
	}
}
