package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_040_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("demands", "52_40");
					put("demands2", "52_40");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, damage +1 and nothing else
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc; a regroup ability only used on purpose
					put("whu", "18_131");       // White Hand Uruk: [uruk-hai] Uruk-hai; its play requirement does not matter when cheated in
					put("assassin", "2_93");    // Tower Assassin: [sauron] minion; "Maneuver: Exert Tower Assassin to wound an ally."

					put("celeborn", "1_34");    // Celeborn: [elven] Elf ally, vitality 3; "Fellowship: Exert Celeborn to heal an [elven] ally."
					put("rosie", "1_309");      // Rosie Cotton: [shire] Hobbit ally, vitality 2; Sam is strength +1, and exerts to heal Sam
					put("goldberry", "9_51");   // Goldberry: [shire] ally who is not a Hobbit; at a river, exerts in a skirmish to make a companion strength +1
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion; exerts to remove a burden
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DemandsoftheSackvilleBagginsesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Demands of the Sackville-Bagginses
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot an [isengard] minion.
		 * 	Each time an ally exerts, add (1) (or (2) if that ally is [shire]).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("demands");

		assertEquals("Demands of the Sackville-Bagginses", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CanBePlayedToTheSupportAreaOnlyOnceAnIsengardMinionCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var demands = scn.GetShadowCard("demands");
		var demands2 = scn.GetShadowCard("demands2");
		var savage = scn.GetShadowCard("savage");
		var whu = scn.GetShadowCard("whu");
		scn.MoveCardsToHand(demands, savage);

		scn.StartGame();
		// An [uruk-hai] Uruk-hai and an [isengard] card that is not a minion: neither is an [isengard] minion.
		scn.MoveMinionsToTable(whu);
		scn.MoveCardsToSupportArea(demands2);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(demands));

		scn.ShadowPlayCard(savage);
		assertTrue(scn.ShadowPlayAvailable(demands));

		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(demands);
		assertEquals(twilight - 1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, demands);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnIsengardOrcLetsItBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var demands = scn.GetShadowCard("demands");
		var servant = scn.GetShadowCard("servant");
		scn.MoveCardsToHand(demands);

		scn.StartGame();
		scn.MoveMinionsToTable(servant);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(demands));
	}

	@Test
	public void EachTimeAnAllyExertsAdds1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var celeborn = scn.GetFreepsCard("celeborn");
		var rosie = scn.GetFreepsCard("rosie");
		scn.MoveAlliesToTable(celeborn, rosie);

		var demands = scn.GetShadowCard("demands");
		scn.MoveCardsToSupportArea(demands);

		scn.StartGame();

		// Rosie, a [shire] ally, is on the table but does not exert: "that ally" is the one exerting.
		assertEquals(0, scn.GetTwilight());
		scn.FreepsUseCardAction(celeborn);
		assertEquals(1, scn.GetTwilight());

		scn.FreepsUseCardAction(celeborn);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeAShireAllyExertsAdds2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rosie = scn.GetFreepsCard("rosie");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveAlliesToTable(rosie, celeborn);

		var demands = scn.GetShadowCard("demands");
		scn.MoveCardsToSupportArea(demands);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(rosie));
		scn.FreepsUseCardAction(rosie);
		assertEquals(1, scn.GetWoundsOn(rosie));
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AShireAllyWhoIsNotAHobbitAlsoAdds2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goldberry = scn.GetFreepsCard("goldberry");
		var frodo = scn.GetRingBearer();
		var demands = scn.GetShadowCard("demands");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		// Goldberry's ability needs the fellowship at a river: Anduin Banks, site 8.
		// Site 7 discards every ally on arrival, so she is put down once the fellowship is there.
		scn.SkipToSite(7);
		scn.MoveAlliesToTable(goldberry);
		scn.MoveCardsToSupportArea(demands);
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, savage);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(goldberry));
		scn.FreepsUseCardAction(goldberry);
		assertEquals(1, scn.GetWoundsOn(goldberry));
		assertEquals(twilight + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ACompanionOrMinionExertingOrAnAllyBeingWoundedAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var rosie = scn.GetFreepsCard("rosie");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(rosie, celeborn);

		var demands = scn.GetShadowCard("demands");
		var assassin = scn.GetShadowCard("assassin");
		scn.MoveCardsToSupportArea(demands);

		scn.StartGame();
		scn.AddBurdens(1);

		// Sam is [shire] but a companion, not an ally.
		assertEquals(0, scn.GetTwilight());
		scn.FreepsUseCardAction(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetTwilight());

		scn.MoveMinionsToTable(assassin);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// A minion exerting, and a [shire] ally taking a wound that is not an exertion.
		int twilight = scn.GetTwilight();
		scn.ShadowUseCardAction(assassin);
		assertTrue(scn.ShadowHasCardChoiceAvailable(rosie, celeborn));
		scn.ShadowChooseCard(rosie);
		assertEquals(1, scn.GetWoundsOn(assassin));
		assertEquals(1, scn.GetWoundsOn(rosie));
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
