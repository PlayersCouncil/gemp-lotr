package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_286_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rider", "4_286");
					put("rider2", "4_286");

					// Firefoot, Mearas of The Mark and Snowmane, Noble Mearas: [rohan] mounts, "Bearer must be a
					// [rohan] Man"; their play triggers are not set off by a cheat, and their other texts need
					// Éomer or Théoden and [rohan] tokens.
					put("firefoot", "13_126");
					put("snowmane", "13_136");
					// Heavy Chain: armor, "Bearer must be a Man"; a possession that is no mount.
					put("chain", "4_278");

					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] Dwarf, unbound; a skirmish ability used only on purpose
					put("boromir", "0_65");     // Boromir, Steward's Heir: [gondor] Man, Ring-bound and not the Ring-bearer; keywords only
					put("thrarin", "1_27");     // Thrarin: [dwarven] ally; "Maneuver: Exert Thrarin to allow him to participate in ... skirmishes"

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 only
					put("scout", "1_191");      // Moria Scout: [moria] Orc, strength 6, vitality 2; its play trigger is not set off by a cheat
					put("scout2", "1_191");
					put("scout3", "1_191");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RiderofRohanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Rider of Rohan
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Skirmish:</b> If this companion is mounted, exert him to exert a minion skirmishing an unbound companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rider");

		assertEquals("Rider of Rohan", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void MountedHeExertsToExertAMinionSkirmishingAnUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetFreepsCard("rider");
		var rider2 = scn.GetFreepsCard("rider2");
		var firefoot = scn.GetFreepsCard("firefoot");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(rider, rider2, gimli);
		scn.AttachCardsTo(rider, firefoot);

		var savage = scn.GetShadowCard("savage");
		var scout = scn.GetShadowCard("scout");
		var scout2 = scn.GetShadowCard("scout2");
		var scout3 = scn.GetShadowCard("scout3");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, scout, scout2, scout3);
		// Vitality 2 with 1 wound: exhausted, so it cannot be exerted.
		scn.AddWoundsToChar(scout3, 1);
		assertEquals(1, scn.GetWoundsOn(scout3));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{rider2, savage}, new PhysicalCardImpl[]{gimli, scout2});
		// The Shadow player puts both other Scouts on the other Rider too, beside the Savage.
		scn.ShadowAssignToMinions(rider2, scout, scout3);

		// The other Rider's skirmish first: Gimli's Scout waits, its skirmish not begun.
		scn.FreepsResolveSkirmish(rider2);
		assertEquals(0, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(rider2));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(scout2));
		// He is in no skirmish himself.
		assertTrue(scn.FreepsActionAvailable(rider));
		scn.FreepsUseCardAction(rider);

		// "Exert him": this Rider, not the other copy.
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(rider2));
		// An Uruk-hai and an Orc of two cultures skirmishing an unbound companion; not the Scout only assigned,
		// nor the exhausted one, who cannot exert.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, scout));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(scout2, scout3));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(scout2));
		assertEquals(1, scn.GetWoundsOn(scout3));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Gimli, an unbound companion of another culture and race, in his own skirmish.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.FreepsActionAvailable(rider));
		scn.FreepsUseCardAction(rider);

		// The one minion skirmishing him is exerted with nothing asked.
		assertEquals(2, scn.GetWoundsOn(rider));
		assertEquals(1, scn.GetWoundsOn(scout2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InTheSkirmishOfARingBoundCompanionOrOfAnAllyItExertsNoMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetFreepsCard("rider");
		var firefoot = scn.GetFreepsCard("firefoot");
		var boromir = scn.GetFreepsCard("boromir");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(rider, boromir);
		scn.AttachCardsTo(rider, firefoot);
		scn.MoveAlliesToTable(thrarin);

		var savage = scn.GetShadowCard("savage");
		var scout = scn.GetShadowCard("scout");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, scout);
		scn.SkipToPhase(Phase.MANEUVER);
		// Thrarin takes part in skirmishes until the regroup phase.
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, savage}, new PhysicalCardImpl[]{thrarin, scout});

		// Boromir is Ring-bound: the Savage skirmishing him is not skirmishing an unbound companion.
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(0, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(savage));
		// The cost can be paid, so the ability is offered there; the effect finds no minion.
		assertTrue(scn.FreepsActionAvailable(rider));
		scn.FreepsUseCardAction(rider);
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(scout));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// Thrarin is an ally, not a companion.
		scn.FreepsResolveSkirmish(thrarin);
		assertEquals(0, scn.GetWoundsOn(scout));
		assertTrue(scn.FreepsActionAvailable(rider));
		scn.FreepsUseCardAction(rider);
		assertEquals(2, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(scout));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileHeIsNotMountedThoughAnotherRiderIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetFreepsCard("rider");
		var rider2 = scn.GetFreepsCard("rider2");
		var chain = scn.GetFreepsCard("chain");
		var snowmane = scn.GetFreepsCard("snowmane");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(rider, rider2, gimli);
		// He bears a possession that is no mount; the other copy is mounted.
		scn.AttachCardsTo(rider, chain);
		scn.AttachCardsTo(rider2, snowmane);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(0, scn.GetWoundsOn(rider));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(rider));
		assertTrue(scn.FreepsActionAvailable(rider2));
	}

	@Test
	public void IsNotOfferedWhileHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetFreepsCard("rider");
		var firefoot = scn.GetFreepsCard("firefoot");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(rider, gimli);
		scn.AttachCardsTo(rider, firefoot);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		// Vitality 3 with 2 wounds: exhausted, so he cannot exert.
		scn.AddWoundsToChar(rider, 2);
		assertEquals(2, scn.GetWoundsOn(rider));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(rider));
		assertEquals(0, scn.GetWoundsOn(savage));
	}
}
