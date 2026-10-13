package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_018_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hosts", "2_18");
					put("hosts2", "2_18");

					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, vitality 3; her text is about Nazgûl
					put("lorienelf", "1_53");   // Lórien Elf: Elf companion, cost 1, vitality 2; "To play, spot an Elf."
					put("gimli", "1_13");       // Gimli, Son of Glóin: a Dwarf companion; a skirmish ability used only on purpose
					// Elrond, Lord of Rivendell: an Elf ally, vitality 4.  His start-of-turn heal finds nobody wounded,
					// and his fellowship ability is never used.
					put("elrond", "1_40");
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: a Dwarf ally, vitality 2; a maneuver ability never used

					put("marksman1", "1_176");  // Goblin Marksman: Archer is its whole text
					put("marksman2", "1_176");
					put("marksman3", "1_176");
					put("assassin", "2_93");    // Tower Assassin: vitality 3; "Maneuver: Exert Tower Assassin to wound an ally."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HostsoftheLastAllianceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Hosts of the Last Alliance
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Game Text: Tale. Plays to your support area.
		 * 	While you can spot an Elf companion, the minion archery total is -1.
		 * 	Response: If an Elf is about to take a wound, discard this condition to prevent that wound.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hosts");

		assertEquals("Hosts of the Last Alliance", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MinionArcheryTotalIsMinusOneOnceAnElfCompanionCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hosts = scn.GetFreepsCard("hosts");
		var gimli = scn.GetFreepsCard("gimli");
		var elrond = scn.GetFreepsCard("elrond");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToSupportArea(hosts);
		scn.MoveCardsToHand(lorienelf);

		scn.StartGame();
		scn.MoveMinionsToTable("marksman1", "marksman2");

		// An Elf who is an ally and a companion who is no Elf: no Elf companion to spot, and two archers count 2.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertTrue(scn.FreepsPlayAvailable(lorienelf));

		// Elrond is the Elf the Lórien Elf's play spots.
		scn.FreepsPlayCard(lorienelf);
		assertInZone(Zone.FREE_CHARACTERS, lorienelf);
		assertEquals(1, scn.GetShadowArcheryTotal());

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(1, scn.GetShadowArcheryTotal());
	}

	@Test
	public void EitherCopyIsDiscardedToPreventThatWoundToAnElfCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hosts = scn.GetFreepsCard("hosts");
		var hosts2 = scn.GetFreepsCard("hosts2");
		var arwen = scn.GetFreepsCard("arwen");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(arwen, lorienelf, gimli);
		scn.MoveCardsToSupportArea(hosts, hosts2);

		scn.StartGame();
		scn.MoveMinionsToTable("marksman1", "marksman2", "marksman3");

		// Three archers, and each copy is -1 while an Elf companion can be spotted: Arwen this time.
		scn.SkipToArcheryWounds();
		assertEquals(1, scn.GetShadowArcheryTotal());

		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(lorienelf));
		assertEquals(0, scn.GetWoundsOn(gimli));

		// The arrow is for Arwen: each copy offers itself.
		scn.FreepsChooseCard(arwen);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hosts));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hosts2));
		scn.FreepsChooseOptionalTrigger(hosts);

		// The copy used is the one discarded; the other stays.  The wound is not placed on Arwen, and the
		// other Elf, who was not about to be wounded, is untouched.
		assertInZone(Zone.DISCARD, hosts);
		assertInZone(Zone.SUPPORT, hosts2);
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(lorienelf));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotOfferedForAWoundToACompanionWhoIsNoElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hosts = scn.GetFreepsCard("hosts");
		var arwen = scn.GetFreepsCard("arwen");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(arwen, gimli);
		scn.MoveCardsToSupportArea(hosts);

		scn.StartGame();
		scn.MoveMinionsToTable("marksman1", "marksman2");

		// Two archers, -1 for Arwen: one arrow.
		scn.SkipToArcheryWounds();
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.FreepsChooseCard(gimli);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(hosts));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertInZone(Zone.SUPPORT, hosts);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void DiscardedToPreventAWoundToAnElfWhoIsAnAllyAndNotOfferedForAnotherAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hosts = scn.GetFreepsCard("hosts");
		var arwen = scn.GetFreepsCard("arwen");
		var elrond = scn.GetFreepsCard("elrond");
		var thrarin = scn.GetFreepsCard("thrarin");
		var assassin = scn.GetShadowCard("assassin");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(elrond, thrarin);
		scn.MoveCardsToSupportArea(hosts);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(assassin));
		assertEquals(0, scn.GetWoundsOn(thrarin));

		// A Dwarf ally first: not offered.
		scn.ShadowUseCardAction(assassin);
		assertTrue(scn.ShadowHasCardChoiceAvailable(elrond, thrarin));
		scn.ShadowChooseCard(thrarin);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(hosts));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertInZone(Zone.SUPPORT, hosts);

		// Then Elrond, an Elf who is an ally: offered, and the wound is prevented.
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(elrond));
		scn.ShadowUseCardAction(assassin);
		scn.ShadowChooseCard(elrond);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hosts));
		scn.FreepsAcceptOptionalTrigger();

		assertInZone(Zone.DISCARD, hosts);
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(2, scn.GetWoundsOn(assassin));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
