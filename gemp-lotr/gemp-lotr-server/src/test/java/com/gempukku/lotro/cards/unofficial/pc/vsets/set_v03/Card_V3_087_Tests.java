package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_087_Tests
{

	// Northern Signal-fire, Flame of Halifirien (103_87) Tests

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("halifirien", "103_87");
					put("beacon1", "103_35");
					put("beacon2", "103_35");
					put("beacon3", "103_35");

					put("eowyn", "5_122");        // Rohan Man for mount bearer
					put("ridermount", "4_287");   // Rohan mount, exerts minions at skirmish start
					put("brego", "4_263");        // Mount with exert + heal on play
					put("riderofrohan", "4_286"); // Companion with exert ability (not a mount)

					put("gollum", "9_28");        // 5 str, 4 vit minion for exertion tests
					put("runner", "1_178");       // Generic minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NorthernSignalfireStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Northern Signal-fire, Flame of Halifirien
		 * Unique: 2
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support area
		 * Game Text: Beacon. To play, hinder 2 beacons.
		* 	Each time you play a mount on your Man, you may add (1) to heal that Man.
		* 	Response: If a mount borne by your Man exerts a minion, hinder this beacon to exert that minion again.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("halifirien");

		assertEquals("Northern Signal-fire", card.getBlueprint().getTitle());
		assertEquals("Flame of Halifirien", card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.BEACON));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}


//
// Extra Cost tests - requires hindering 2 beacons
//

	@Test
	public void HalifirienCannotPlayWithoutTwoBeaconsToHinder() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halifirien = scn.GetFreepsCard("halifirien");
		var beacon1 = scn.GetFreepsCard("beacon1");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCardsToHand(halifirien);
		scn.MoveCompanionsToTable(eowyn);
		scn.MoveCardsToSupportArea(beacon1);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(halifirien));
	}

	@Test
	public void HalifirienCanPlayByHinderingTwoBeacons() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halifirien = scn.GetFreepsCard("halifirien");
		var beacon1 = scn.GetFreepsCard("beacon1");
		var beacon2 = scn.GetFreepsCard("beacon2");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCardsToHand(halifirien);
		scn.MoveCompanionsToTable(eowyn);
		scn.MoveCardsToSupportArea(beacon1, beacon2);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(halifirien));

		scn.FreepsPlayCard(halifirien);
		scn.FreepsChooseCards(beacon1, beacon2);

		assertInZone(Zone.SUPPORT, halifirien);
		assertTrue(scn.IsHindered(beacon1));
		assertTrue(scn.IsHindered(beacon2));
	}

//
// Mount play trigger tests - add (1) to heal bearer
//

	@Test
	public void HalifirienTriggersWhenMountPlayedOnYourMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halifirien = scn.GetFreepsCard("halifirien");
		var ridermount = scn.GetFreepsCard("ridermount");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCardsToHand(ridermount);
		scn.MoveCompanionsToTable(eowyn);
		scn.MoveCardsToSupportArea(halifirien);
		scn.AddWoundsToChar(eowyn, 1);

		scn.StartGame();

		assertEquals(1, scn.GetWoundsOn(eowyn));

		scn.FreepsPlayCard(ridermount);
		// Mount auto-attaches to only valid target

		int twilight = scn.GetTwilight();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable("Halifirien"));

		scn.FreepsAcceptOptionalTrigger();
		// Bearer auto-selected for heal

		// Twilight added as cost, Eowyn healed
		assertEquals(twilight + 1, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(eowyn));
	}

	@Test
	public void HalifirienTriggerCanBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halifirien = scn.GetFreepsCard("halifirien");
		var ridermount = scn.GetFreepsCard("ridermount");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCardsToHand(ridermount);
		scn.MoveCompanionsToTable(eowyn);
		scn.MoveCardsToSupportArea(halifirien);
		scn.AddWoundsToChar(eowyn, 1);

		scn.StartGame();

		scn.FreepsPlayCard(ridermount);

		int twilight = scn.GetTwilight();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable("Halifirien"));

		scn.FreepsDeclineOptionalTrigger();

		// No twilight added, Eowyn not healed
		assertEquals(twilight, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(eowyn));
	}

	@Test
	public void HalifirienAndBregoTriggersBothFireWhenBregoPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halifirien = scn.GetFreepsCard("halifirien");
		var brego = scn.GetFreepsCard("brego");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCardsToHand(brego);
		scn.MoveCompanionsToTable(eowyn);
		scn.MoveCardsToSupportArea(halifirien);
		scn.AddWoundsToChar(eowyn, 2);

		scn.StartGame();

		assertEquals(2, scn.GetWoundsOn(eowyn));

		scn.FreepsPlayCard(brego);
		// Mount auto-attaches to only valid Man target
		int twilight = scn.GetTwilight();

		// Both Brego and Halifirien trigger - choose which to resolve first
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsUseCardAction(brego); // Resolve Brego's heal first

		assertEquals(1, scn.GetWoundsOn(eowyn)); // Healed once by Brego

		// Now Halifirien's trigger
		assertTrue(scn.FreepsHasOptionalTriggerAvailable("Halifirien"));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(twilight + 1, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(eowyn)); // Healed again by Halifirien
	}

//
// Response tests - mount exerts minion, hinder beacon to exert again
//

	@Test
	public void HalifirienResponseTriggersWhenMountExertsMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halifirien = scn.GetFreepsCard("halifirien");
		var beacon1 = scn.GetFreepsCard("beacon1");
		var ridermount = scn.GetFreepsCard("ridermount");
		var eowyn = scn.GetFreepsCard("eowyn");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCompanionsToTable(eowyn);
		scn.AttachCardsTo(eowyn, ridermount);
		scn.MoveCardsToSupportArea(halifirien, beacon1);
		scn.MoveMinionsToTable(gollum);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eowyn, gollum);

		assertEquals(0, scn.GetWoundsOn(gollum));
		assertFalse(scn.IsHindered(beacon1));

		scn.FreepsResolveSkirmish(eowyn);
		// Rider's Mount exerts Gollum at start of skirmish (required)

		assertEquals(1, scn.GetWoundsOn(gollum));

		// Halifirien response should be available
		assertTrue(scn.FreepsHasOptionalTriggerAvailable("Halifirien"));

		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(beacon1);

		// Gollum exerted again, beacon hindered
		assertEquals(2, scn.GetWoundsOn(gollum));
		assertTrue(scn.IsHindered(beacon1));
	}

	@Test
	public void HalifirienResponseCanBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halifirien = scn.GetFreepsCard("halifirien");
		var beacon1 = scn.GetFreepsCard("beacon1");
		var ridermount = scn.GetFreepsCard("ridermount");
		var eowyn = scn.GetFreepsCard("eowyn");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCompanionsToTable(eowyn);
		scn.AttachCardsTo(eowyn, ridermount);
		scn.MoveCardsToSupportArea(halifirien, beacon1);
		scn.MoveMinionsToTable(gollum);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eowyn, gollum);
		scn.FreepsResolveSkirmish(eowyn);

		assertEquals(1, scn.GetWoundsOn(gollum));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable("Halifirien"));

		scn.FreepsDeclineOptionalTrigger();

		// Gollum only exerted once, beacon not hindered
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertFalse(scn.IsHindered(beacon1));
	}

	@Test
	public void HalifirienResponseDoesNotTriggerWhenCompanionExertsMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halifirien = scn.GetFreepsCard("halifirien");
		var beacon1 = scn.GetFreepsCard("beacon1");
		var ridermount = scn.GetFreepsCard("ridermount");
		var riderofrohan = scn.GetFreepsCard("riderofrohan");
		var eowyn = scn.GetFreepsCard("eowyn");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCompanionsToTable(eowyn, riderofrohan);
		scn.AttachCardsTo(riderofrohan, ridermount); // Mount on Rider of Rohan
		scn.MoveCardsToSupportArea(halifirien, beacon1);
		scn.MoveMinionsToTable(gollum);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eowyn, gollum);
		scn.FreepsResolveSkirmish(eowyn);

		assertEquals(0, scn.GetWoundsOn(gollum)); // Eowyn not mounted, no exert

		// Use Rider of Rohan's skirmish ability to exert Gollum
		assertTrue(scn.FreepsActionAvailable(riderofrohan));
		scn.FreepsUseCardAction(riderofrohan);
		// Gollum auto-selected as only minion skirmishing unbound companion

		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(1, scn.GetWoundsOn(riderofrohan)); // Rider exerted himself as cost

		// Halifirien should NOT trigger - source was companion, not mount
		assertFalse(scn.FreepsHasOptionalTriggerAvailable("Halifirien"));
		assertFalse(scn.IsHindered(beacon1));
	}

}
