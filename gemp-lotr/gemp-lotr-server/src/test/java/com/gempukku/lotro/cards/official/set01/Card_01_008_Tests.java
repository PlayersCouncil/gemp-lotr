package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_008_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armor", "1_8");
					put("armor2", "1_8");

					put("gimli", "1_13");       // Gimli, Son of Gloin: unique Dwarf companion; a skirmish ability used only on purpose
					put("guard1", "1_7");       // Dwarf Guard: Dwarf companion, not unique, strength 4, vitality 2; "To play, spot a Dwarf" only
					put("guard2", "1_7");
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, home site 3, strength 3, vitality 3; a fellowship ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, home site 3, strength 4, vitality 2; a maneuver ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion; a maneuver ability used only on purpose

					put("brigand1", "4_10");    // Dunlending Brigand: Man minion, strength 8, vitality 1, no text
					put("brigand2", "4_10");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DwarvenArmorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Dwarven Armor
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Armor
		 * Game Text: Bearer must be a Dwarf.<br>Bearer may not be overwhelmed unless his or her strength is tripled.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("armor");

		assertEquals("Dwarven Armor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.ARMOR));
		assertEquals(1, card.getBlueprint().getPossessionClasses().size());
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnADwarfCompanionOrADwarfAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard1");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(armor);
		scn.MoveCompanionsToTable(gimli, guard, aragorn);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();

		assertInHand(armor);
		assertTrue(scn.FreepsPlayAvailable(armor));
		scn.FreepsPlayCard(armor);
		// Dwarf companions of two titles, one unique and one not, and a Dwarf ally; not a Man, nor a Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, guard, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(grimir);

		assertAttachedTo(armor, grimir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearerIsNotOverwhelmedAtDoubleStrengthAndAnotherDwarfIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var brigand1 = scn.GetShadowCard("brigand1");
		var brigand2 = scn.GetShadowCard("brigand2");
		scn.MoveCompanionsToTable(guard1, guard2);
		scn.AttachCardsTo(guard1, armor);

		scn.StartGame();
		assertAttachedTo(armor, guard1);
		scn.MoveMinionsToTable(brigand1, brigand2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard1, brigand1}, new PhysicalCardImpl[]{guard2, brigand2});

		// 8 against 4: exactly double, less than triple.  The Armor spares him; he takes the 1 wound for losing.
		scn.FreepsResolveSkirmish(guard1);
		assertAttachedTo(armor, guard1);
		assertEquals(4, scn.GetStrength(guard1));
		assertEquals(8, scn.GetStrength(brigand1));
		assertEquals(0, scn.GetWoundsOn(guard1));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard1));
		assertInPlay(guard1);

		// The same against the other Guard, who bears no Armor: overwhelmed.
		scn.FreepsResolveSkirmish(guard2);
		assertEquals(4, scn.GetStrength(guard2));
		assertEquals(8, scn.GetStrength(brigand2));
		assertEquals(0, scn.GetFreepsDeadCount());
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard2);
		assertEquals(1, scn.GetFreepsDeadCount());

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ABearerWhoIsAnAllyIsNotOverwhelmedAtDoubleStrengthAndAnotherDwarfAllyIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var grimir = scn.GetFreepsCard("grimir");
		var thrarin = scn.GetFreepsCard("thrarin");
		var brigand1 = scn.GetShadowCard("brigand1");
		var brigand2 = scn.GetShadowCard("brigand2");
		scn.MoveAlliesToTable(grimir, thrarin);
		scn.AttachCardsTo(grimir, armor);

		scn.StartGame();
		assertAttachedTo(armor, grimir);
		scn.SkipToSite(2);
		scn.FreepsPass();
		// Site 3: the home of both allies, where they may be assigned.
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(brigand1, brigand2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{grimir, brigand1}, new PhysicalCardImpl[]{thrarin, brigand2});

		// 8 against 3: more than double, less than triple.  The Armor spares the ally; he takes the 1 wound for losing.
		scn.FreepsResolveSkirmish(grimir);
		assertAttachedTo(armor, grimir);
		assertEquals(3, scn.GetStrength(grimir));
		assertEquals(8, scn.GetStrength(brigand1));
		assertEquals(0, scn.GetWoundsOn(grimir));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertInPlay(grimir);

		// 8 against 4, exactly double, on the other Dwarf ally, who bears no Armor: overwhelmed.
		scn.FreepsResolveSkirmish(thrarin);
		assertEquals(4, scn.GetStrength(thrarin));
		assertEquals(8, scn.GetStrength(brigand2));
		assertEquals(0, scn.GetFreepsDeadCount());
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, thrarin);
		assertEquals(1, scn.GetFreepsDeadCount());

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BearerIsOverwhelmedAtExactlyTripleStrengthAndNotOneShortOfIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var armor2 = scn.GetFreepsCard("armor2");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var brigand1 = scn.GetShadowCard("brigand1");
		var brigand2 = scn.GetShadowCard("brigand2");
		scn.MoveCompanionsToTable(guard1, guard2);
		scn.AttachCardsTo(guard1, armor);
		scn.AttachCardsTo(guard2, armor2);

		scn.StartGame();
		assertAttachedTo(armor, guard1);
		assertAttachedTo(armor2, guard2);
		scn.MoveMinionsToTable(brigand1, brigand2);
		// Strengths that only set the board: 8 + 3 = 11, one short of triple a Guard's 4; 8 + 4 = 12, exactly triple.
		scn.ApplyAdHocModifier(new StrengthModifier(null, brigand1, null, 3));
		scn.ApplyAdHocModifier(new StrengthModifier(null, brigand2, null, 4));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard1, brigand1}, new PhysicalCardImpl[]{guard2, brigand2});

		scn.FreepsResolveSkirmish(guard1);
		assertAttachedTo(armor, guard1);
		assertEquals(4, scn.GetStrength(guard1));
		assertEquals(11, scn.GetStrength(brigand1));
		assertEquals(0, scn.GetWoundsOn(guard1));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard1));
		assertInPlay(guard1);

		scn.FreepsResolveSkirmish(guard2);
		assertAttachedTo(armor2, guard2);
		assertEquals(4, scn.GetStrength(guard2));
		assertEquals(12, scn.GetStrength(brigand2));
		assertEquals(0, scn.GetWoundsOn(guard2));
		assertEquals(0, scn.GetFreepsDeadCount());
		scn.PassSkirmishActions();
		// Vitality 2 and unwounded: the one wound for losing would not kill him; the overwhelm does.
		assertInZone(Zone.DEAD, guard2);
		assertEquals(1, scn.GetFreepsDeadCount());

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
