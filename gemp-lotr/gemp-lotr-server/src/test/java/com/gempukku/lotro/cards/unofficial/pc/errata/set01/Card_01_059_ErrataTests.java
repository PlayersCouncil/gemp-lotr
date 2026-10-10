package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_059_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sts", "51_59");

					put("gimli", "1_13");         // Gimli, Son of Gloin: Dwarf companion, vitality 3
					put("grimir", "1_17");        // Grimir, Dwarven Elder: Dwarf ally, vitality 3; a fellowship ability of his own, never used here
					put("legolas", "1_50");       // Legolas, Greenleaf: Elf companion, vitality 3; an archery ability only
					put("rumil", "1_57");         // Rumil, Elven Protector: Elf ally, vitality 3; an archery ability only
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a Man, vitality 4
					put("farin", "1_11");         // Farin, Dwarven Emissary: Dwarf companion, vitality 3; a requirement to play, and strength against an Orc
					put("arwen", "1_30");         // Arwen, Daughter of Elrond: Elf companion, vitality 3; strength against a Nazgul only

					put("runner", "1_178");       // Goblin Runner: a minion on the table, so that there is a maneuver phase
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
		 * Game Text: <b>Fellowship:</b> Add (1) and exert a Dwarf to heal an Elf, or add (1) and exert an Elf
		 * to heal a Dwarf.
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
	public void IsPlayedFromHandForOneToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sts = scn.GetFreepsCard("sts");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(gimli, legolas);
		scn.MoveCardsToHand(sts);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.HAND, sts);
		assertTrue(scn.FreepsPlayAvailable(sts));
		assertFalse(scn.FreepsActionAvailable(sts));

		scn.FreepsPlayCard(sts);
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, sts);
		assertNotAttachedTo(sts, gimli);
		assertNotAttachedTo(sts, legolas);
		assertTrue(scn.FreepsActionAvailable(sts));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertingADwarfAddsOneAndHealsAnElfOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sts = scn.GetFreepsCard("sts");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var legolas = scn.GetFreepsCard("legolas");
		var rumil = scn.GetFreepsCard("rumil");
		var aragorn = scn.GetFreepsCard("aragorn");
		var farin = scn.GetFreepsCard("farin");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(gimli, legolas, aragorn, farin, arwen);
		scn.MoveAlliesToTable(grimir, rumil);
		scn.MoveCardsToSupportArea(sts);
		// Three Dwarves and three Elves, companions and allies, and Aragorn, who is neither. All are wounded.
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(grimir, 1);
		scn.AddWoundsToChar(legolas, 1);
		scn.AddWoundsToChar(rumil, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(farin, 1);
		scn.AddWoundsToChar(arwen, 1);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(rumil));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(farin));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsActionAvailable(sts));

		// Who exerts: any Dwarf or Elf, companion or ally, and nobody else.
		scn.FreepsUseCardAction(sts);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir, farin, legolas, rumil, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(gimli));

		// A Dwarf exerted, so an Elf is healed: not another Dwarf, not the Dwarf who exerted, not the Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, rumil, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, grimir, farin, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(rumil);
		assertEquals(0, scn.GetWoundsOn(rumil));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(farin));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Gimli, vitality 3 with 2 wounds, is exhausted and cannot exert again. Farin does, and Arwen is
		// healed; Rumil, with no wound left, is not among the Elves to heal.
		scn.FreepsUseCardAction(sts);
		assertTrue(scn.FreepsHasCardChoiceAvailable(grimir, farin, legolas, rumil, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, aragorn));

		scn.FreepsChooseCard(farin);
		assertEquals(2, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(farin));
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(rumil, gimli, grimir, farin, aragorn));

		scn.FreepsChooseCard(arwen);
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A Dwarf ally exerts. Legolas is the one wounded Elf left, so he is healed without a question.
		scn.FreepsUseCardAction(sts);
		scn.FreepsChooseCard(grimir);
		assertEquals(3, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertingAnElfAddsOneAndHealsADwarfOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sts = scn.GetFreepsCard("sts");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var legolas = scn.GetFreepsCard("legolas");
		var rumil = scn.GetFreepsCard("rumil");
		var aragorn = scn.GetFreepsCard("aragorn");
		var farin = scn.GetFreepsCard("farin");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(gimli, legolas, aragorn, farin, arwen);
		scn.MoveAlliesToTable(grimir, rumil);
		scn.MoveCardsToSupportArea(sts);
		// Three Dwarves and three Elves, companions and allies, and Aragorn, who is neither. All are wounded.
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(grimir, 1);
		scn.AddWoundsToChar(legolas, 1);
		scn.AddWoundsToChar(rumil, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(farin, 1);
		scn.AddWoundsToChar(arwen, 1);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(rumil));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(farin));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsActionAvailable(sts));

		scn.FreepsUseCardAction(sts);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir, farin, legolas, rumil, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(legolas);
		assertEquals(1, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(legolas));

		// An Elf exerted, so a Dwarf is healed: not another Elf, not the Elf who exerted, not the Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir, farin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, rumil, arwen, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(gimli);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(farin));
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(rumil));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Legolas, vitality 3 with 2 wounds, is exhausted and cannot exert again. Arwen does, and Farin is
		// healed; Gimli, with no wound left, is not among the Dwarves to heal.
		scn.FreepsUseCardAction(sts);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir, farin, rumil, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, aragorn));

		scn.FreepsChooseCard(arwen);
		assertEquals(2, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsHasCardChoiceAvailable(grimir, farin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, legolas, rumil, arwen, aragorn));

		scn.FreepsChooseCard(farin);
		assertEquals(0, scn.GetWoundsOn(farin));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// An Elf ally exerts. Grimir is the one wounded Dwarf left, so he is healed without a question.
		scn.FreepsUseCardAction(sts);
		scn.FreepsChooseCard(rumil);
		assertEquals(3, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(rumil));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
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

		// Aragorn, a Man with no wound on him, cannot exert in their place.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsActionAvailable(sts));

		scn.RemoveWoundsFromChar(legolas, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(sts));
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(2, scn.GetWoundsOn(gimli));

		// Only the Elf can exert and so only the Dwarf can be healed: neither is asked about.
		scn.FreepsUseCardAction(sts);
		assertEquals(1, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Now only the Dwarf can exert, and the Elf is healed.
		assertTrue(scn.FreepsActionAvailable(sts));
		scn.FreepsUseCardAction(sts);
		assertEquals(2, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
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

		assertEquals(0, scn.GetTwilight());
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

		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
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

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(rumil));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsActionAvailable(sts));

		// Gimli could exert and heal Rumil. An Elf may exert all the same, for no heal.
		scn.FreepsUseCardAction(sts);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, legolas, rumil));
		scn.FreepsChooseCard(legolas);

		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(rumil));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsOfferedInTheFellowshipPhaseAndNotInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sts = scn.GetFreepsCard("sts");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var legolas = scn.GetFreepsCard("legolas");
		var rumil = scn.GetFreepsCard("rumil");
		scn.MoveCompanionsToTable(gimli, legolas);
		scn.MoveAlliesToTable(grimir, rumil);
		scn.MoveCardsToSupportArea(sts);
		// Everyone can exert and everyone has a wound to heal.
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(grimir, 1);
		scn.AddWoundsToChar(legolas, 1);
		scn.AddWoundsToChar(rumil, 1);

		scn.StartGame();
		scn.MoveMinionsToTable("runner");

		assertTrue(scn.FreepsActionAvailable(sts));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertFalse(scn.FreepsActionAvailable(sts));
		// Nobody on this board has a maneuver action of his own either.
		assertFalse(scn.FreepsAnyActionsAvailable());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
