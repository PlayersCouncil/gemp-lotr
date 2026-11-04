package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_070_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ride", "17_70");

					// Denizen of Khazad-dum: an [orc] Orc, strength 9, vitality 2; +2 at an underground site only
					put("denizen", "11_115");
					// Rider's Gear: [orc] armor (+2) for an [orc] Orc, a possession that is no mount; its wound bar
					// holds only while its bearer is mounted
					put("gear", "17_90");
					// Orkish Cavalry: an [orc] Orc, strength 8, vitality 2; a draw when an [orc] mount is played, and an
					// Assignment ability that is not used
					put("cavalry", "17_74");
					// Threatening Warg: an [orc] mount (+2) for an [orc] Orc of strength 10 or less; bearer is fierce;
					// its play trigger is not reached (cheated on)
					put("warg", "17_91");
					// Half-troll of Far Harad: an [orc] minion who is no Orc, strength 8; its start-of-assignment
					// trigger needs more companions than minions
					put("halftroll", "14_12");
					// Mordor Brute: a [sauron] Orc, strength 9; a [sauron] Orc bearing a possession takes no wounds
					// outside skirmishes
					put("brute", "10_90");

					// Aragorn, Ranger of the North: strength 8, vitality 4; his Maneuver ability is not used
					put("aragorn", "1_89");
					// Gimli, Son of Gloin: strength 6, vitality 3; his Skirmish ability is not used
					put("gimli", "1_13");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FeralRideStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Feral Ride
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make an [orc] minion strength +2 (or +4 if it is mounted).
		*/

		var scn = GetScenario();

		var ride = scn.GetFreepsCard("ride");

		assertEquals("Feral Ride", ride.getBlueprint().getTitle());
		assertNull(ride.getBlueprint().getSubtitle());
		assertFalse(ride.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, ride.getBlueprint().getSide());
		assertEquals(Culture.ORC, ride.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, ride.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(ride, Timeword.SKIRMISH));
		assertEquals(1, ride.getBlueprint().getTwilightCost());
	}

	@Test
	public void NotPlayableInTheAssignmentPhaseAndAnOrcMinionThatIsNotMountedIsPlus2ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var ride = scn.GetShadowCard("ride");
		var denizen = scn.GetShadowCard("denizen");
		var gear = scn.GetShadowCard("gear");
		var cavalry = scn.GetShadowCard("cavalry");
		var warg = scn.GetShadowCard("warg");
		var halftroll = scn.GetShadowCard("halftroll");
		var brute = scn.GetShadowCard("brute");
		scn.MoveCardsToHand(ride);
		assertInHand(ride);

		scn.StartGame();
		scn.MoveMinionsToTable(denizen, cavalry, halftroll, brute);
		scn.AttachCardsTo(denizen, gear);
		scn.AttachCardsTo(cavalry, warg);
		assertAttachedTo(gear, denizen);
		assertAttachedTo(warg, cavalry);
		scn.SetTwilight(10);
		assertEquals(10, scn.GetTwilight());

		// The assignment phase, next door to the skirmish: not playable.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(ride));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, denizen}, new PhysicalCardImpl[]{gimli, cavalry});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// 9 and the armour's +2
		assertEquals(11, scn.GetStrength(denizen));
		// 8 and the Warg's +2
		assertEquals(10, scn.GetStrength(cavalry));
		assertEquals(8, scn.GetStrength(halftroll));
		assertTrue(scn.ShadowPlayAvailable(ride));
		scn.ShadowPlayCard(ride);
		// An [orc] Orc in this skirmish, one whose skirmish is still to come, and an [orc] minion who is no Orc and
		// is in no skirmish; not the [sauron] Orc or a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(denizen, cavalry, halftroll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brute, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(denizen);

		assertInDiscard(ride);
		// The Denizen bears armour and no mount, so +2 though a mounted [orc] Orc stands beside him: 11 + 2.
		assertEquals(13, scn.GetStrength(denizen));
		assertEquals(10, scn.GetStrength(cavalry));
		assertEquals(8, scn.GetStrength(halftroll));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The Denizen beats Aragorn; that skirmish was the phase the +2 lasted for.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, cavalry));
		assertEquals(11, scn.GetStrength(denizen));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AMountedOrcMinionIsPlus4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var ride = scn.GetShadowCard("ride");
		var denizen = scn.GetShadowCard("denizen");
		var cavalry = scn.GetShadowCard("cavalry");
		var warg = scn.GetShadowCard("warg");
		scn.MoveCardsToHand(ride);
		assertInHand(ride);

		scn.StartGame();
		scn.MoveMinionsToTable(denizen, cavalry);
		scn.AttachCardsTo(cavalry, warg);
		assertAttachedTo(warg, cavalry);
		scn.SetTwilight(10);
		assertEquals(10, scn.GetTwilight());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, cavalry}, new PhysicalCardImpl[]{gimli, denizen});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(10, scn.GetStrength(cavalry));
		assertEquals(9, scn.GetStrength(denizen));
		scn.ShadowPlayCard(ride);
		scn.ShadowChooseCard(cavalry);

		assertInDiscard(ride);
		// 8, the Warg's +2 and the event's +4
		assertEquals(14, scn.GetStrength(cavalry));
		assertEquals(9, scn.GetStrength(denizen));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
