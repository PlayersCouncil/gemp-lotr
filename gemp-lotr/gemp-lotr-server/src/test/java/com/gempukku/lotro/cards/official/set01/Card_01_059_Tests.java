package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_059_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sts", "1_59");

					put("gimli", "1_13");         // Gimli, Son of Gloin: Dwarf companion, vitality 3; a skirmish ability only
					put("farin", "1_11");         // Farin, Dwarven Emissary: Dwarf companion, vitality 3; a requirement to play, and strength against an Orc
					put("grimir", "1_17");        // Grimir, Dwarven Elder: Dwarf ally, vitality 3; a fellowship ability only
					put("legolas", "1_50");       // Legolas, Greenleaf: Elf companion, vitality 3; an archery ability only
					put("arwen", "1_30");         // Arwen, Daughter of Elrond: Elf companion, vitality 3; strength against a Nazgul only
					put("rumil", "1_57");         // Rumil, Elven Protector: Elf ally, vitality 3; an archery ability only
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a Man, vitality 4; his own maneuver ability is never used here

					put("runner", "1_178");       // Goblin Runner: a minion on the table, so that there is a maneuver phase

					put("finalstrike", "10_20");  // Final Strike: Shadow condition; Response: if the Free Peoples player uses a maneuver or archery special ability, exert Gollum to cancel its effect
					put("gollum", "5_24");        // Gollum, Nasty Treacherous Creature: minion, vitality 4; a regroup ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShouldertoShoulderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Shoulder to Shoulder
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * <b>Maneuver:</b> Add (1) and exert a Dwarf to heal an Elf, or add (1) and exert an Elf to heal a Dwarf.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sts");

		assertEquals("Shoulder to Shoulder", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertingADwarfAddsOneAndHealsAnElfOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sts = scn.GetFreepsCard("sts");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var rumil = scn.GetFreepsCard("rumil");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, farin, legolas, arwen, aragorn);
		scn.MoveAlliesToTable(grimir, rumil);
		scn.MoveCardsToSupportArea(sts);

		scn.StartGame();
		scn.MoveMinionsToTable("runner");
		scn.SkipToPhase(Phase.MANEUVER);

		// Three Dwarves and three Elves, companions and allies, and Aragorn, who is neither. All are wounded;
		// Legolas and Rumil have two wounds, so that one heal leaves one.
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(farin, 1);
		scn.AddWoundsToChar(grimir, 1);
		scn.AddWoundsToChar(legolas, 2);
		scn.AddWoundsToChar(arwen, 1);
		scn.AddWoundsToChar(rumil, 2);
		scn.AddWoundsToChar(aragorn, 1);
		scn.FreepsRefreshDecision();

		int twilight = scn.GetTwilight();
		assertTrue(scn.FreepsActionAvailable(sts));

		// Who exerts: any Dwarf or Elf able to, companion or ally, and nobody else. Legolas and Rumil,
		// vitality 3 with 2 wounds, are exhausted.
		scn.FreepsUseCardAction(sts);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, farin, grimir, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, legolas, rumil, scn.GetRingBearer()));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(grimir);
		assertEquals(twilight + 1, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(grimir));

		// A Dwarf exerted, so an Elf is healed: not another Dwarf, not the Dwarf who exerted, not the Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, arwen, rumil));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, farin, grimir, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(2, scn.GetWoundsOn(rumil));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(farin));
		assertEquals(2, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(twilight + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// A second use: a Dwarf companion exerts and the Elf ally is healed.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(sts));
		scn.FreepsUseCardAction(sts);
		// Legolas, with one wound now, can exert again; Grimir, with two, cannot.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, farin, legolas, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(grimir, rumil, aragorn));

		scn.FreepsChooseCard(gimli);
		assertEquals(twilight + 2, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, arwen, rumil));

		scn.FreepsChooseCard(rumil);
		assertEquals(1, scn.GetWoundsOn(rumil));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ExertingAnElfAddsOneAndHealsADwarfOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sts = scn.GetFreepsCard("sts");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var rumil = scn.GetFreepsCard("rumil");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, farin, legolas, arwen, aragorn);
		scn.MoveAlliesToTable(grimir, rumil);
		scn.MoveCardsToSupportArea(sts);

		scn.StartGame();
		scn.MoveMinionsToTable("runner");
		scn.SkipToPhase(Phase.MANEUVER);

		// The mirror of the board above: Gimli and Grimir have two wounds, everyone else one.
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(farin, 1);
		scn.AddWoundsToChar(grimir, 2);
		scn.AddWoundsToChar(legolas, 1);
		scn.AddWoundsToChar(arwen, 1);
		scn.AddWoundsToChar(rumil, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.FreepsRefreshDecision();

		int twilight = scn.GetTwilight();
		assertTrue(scn.FreepsActionAvailable(sts));

		scn.FreepsUseCardAction(sts);
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, arwen, rumil, farin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, gimli, grimir, scn.GetRingBearer()));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(rumil);
		assertEquals(twilight + 1, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(rumil));

		// An Elf exerted, so a Dwarf is healed: not another Elf, not the Elf who exerted, not the Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, farin, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, arwen, rumil, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(farin));
		assertEquals(2, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(2, scn.GetWoundsOn(rumil));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(twilight + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// A second use: an Elf companion exerts and the Dwarf ally is healed.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(sts));
		scn.FreepsUseCardAction(sts);
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, arwen, gimli, farin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(rumil, grimir, aragorn));

		scn.FreepsChooseCard(legolas);
		assertEquals(twilight + 2, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, farin, grimir));

		scn.FreepsChooseCard(grimir);
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(farin));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedUntilADwarfOrAnElfCanExertAndThenOnlyTheOtherRaceIsHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sts = scn.GetFreepsCard("sts");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, legolas, aragorn);
		scn.MoveCardsToSupportArea(sts);
		// Vitality 3 with 2 wounds: the one Dwarf and the one Elf are both exhausted.
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(legolas, 2);

		scn.StartGame();
		scn.MoveMinionsToTable("runner");
		scn.SkipToPhase(Phase.MANEUVER);

		// Aragorn, a Man with no wound on him, cannot exert in their place.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsActionAvailable(sts));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.RemoveWoundsFromChar(legolas, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(sts));
		int twilight = scn.GetTwilight();
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(2, scn.GetWoundsOn(gimli));

		// Only the Elf can exert and so only the Dwarf can be healed: neither is asked about.
		scn.FreepsUseCardAction(sts);
		assertEquals(twilight + 1, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Now only the Dwarf can exert, and the Elf is healed.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(sts));
		scn.FreepsUseCardAction(sts);
		assertEquals(twilight + 2, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ADwarfMayExertThoughNoElfIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sts = scn.GetFreepsCard("sts");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, legolas, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToSupportArea(sts);
		// The wounded on this board are another Dwarf and a Man.
		scn.AddWoundsToChar(grimir, 1);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();
		scn.MoveMinionsToTable("runner");
		scn.SkipToPhase(Phase.MANEUVER);

		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// The twilight and the exertion are costs and can be paid, so the ability is on offer.
		assertTrue(scn.FreepsActionAvailable(sts));

		// Legolas could exert and heal Grimir. A Dwarf may exert all the same, for no heal.
		scn.FreepsUseCardAction(sts);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir, legolas));
		scn.FreepsChooseCard(gimli);

		assertEquals(twilight + 1, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AnElfMayExertThoughNoDwarfIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sts = scn.GetFreepsCard("sts");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var rumil = scn.GetFreepsCard("rumil");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, legolas, aragorn);
		scn.MoveAlliesToTable(rumil);
		scn.MoveCardsToSupportArea(sts);
		// The wounded on this board are another Elf and a Man.
		scn.AddWoundsToChar(rumil, 1);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();
		scn.MoveMinionsToTable("runner");
		scn.SkipToPhase(Phase.MANEUVER);

		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(rumil));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsActionAvailable(sts));

		// Gimli could exert and heal Rumil. An Elf may exert all the same, for no heal.
		scn.FreepsUseCardAction(sts);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, legolas, rumil));
		scn.FreepsChooseCard(legolas);

		assertEquals(twilight + 1, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(rumil));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WhenFinalStrikeCancelsItTheCostsStayPaidAndNobodyIsHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sts = scn.GetFreepsCard("sts");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(legolas, gimli);
		scn.MoveCardsToSupportArea(sts);
		scn.AddWoundsToChar(legolas, 1);

		var gollum = scn.GetShadowCard("gollum");
		var finalstrike = scn.GetShadowCard("finalstrike");
		scn.MoveCardsToSupportArea(finalstrike);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SkipToPhase(Phase.MANEUVER);

		int twilight = scn.GetTwilight();
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertTrue(scn.FreepsActionAvailable(sts));

		scn.FreepsUseCardAction(sts);
		scn.FreepsChooseCard(gimli);

		// Final Strike answers the ability once its costs are paid, and before Legolas is healed.
		assertEquals(twilight + 1, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(finalstrike));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(gollum));

		// A response is not offered again to an ability it has just cancelled.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(finalstrike));

		// Gimli stays exerted and the twilight stays added; the heal of Legolas does not happen.
		assertEquals(twilight + 1, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
