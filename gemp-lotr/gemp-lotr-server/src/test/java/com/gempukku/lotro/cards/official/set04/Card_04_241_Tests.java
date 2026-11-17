package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_04_241_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("march", "4_241");
					put("axeman", "4_224");     // Easterling Axeman: [raider] Easterling Man, strength 6; +3 and fierce only at 2 burdens
					put("scout", "4_252");      // Southron Scout: [raider] Southron Man, strength 3; ambush (2) only
					put("footmen", "6_3");      // Dunlending Footmen: [dunland] Man, strength 11; a cost text only
					put("rearguard", "103_43"); // Bladetusk Rearguard: [raider] artifact that its maneuver ability makes a Southron minion who is no Man

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6; a skirmish ability only
					put("sam", "1_311");        // Sam, Son of Hamfast
					put("merry", "1_303");      // Merry, From O'er the Brandywine: +2 only while bearing a weapon
					put("pippin", "1_306");     // Pippin, Friend to Frodo: a bar on discarding tales only
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: a Dwarf ally, no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OntheMarchStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: On the March
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make a [raider] Man strength +3 (or +5 if you spot 6 companions).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("march");

		assertEquals("On the March", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SkirmishEventMakesARaiderManStrengthPlusThreeUntilTheSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var march = scn.GetShadowCard("march");
		var axeman = scn.GetShadowCard("axeman");
		var scout = scn.GetShadowCard("scout");
		var footmen = scn.GetShadowCard("footmen");
		var rearguard = scn.GetShadowCard("rearguard");
		scn.MoveCardsToHand(march);
		scn.MoveCardsToSupportArea(rearguard);

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var thrarin = scn.GetFreepsCard("thrarin");
		// Five companions, Frodo among them, and an ally, who is no companion: one short of 6.
		scn.MoveCompanionsToTable(aragorn, gimli, sam, merry);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman, scout, footmen);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();
		scn.ShadowUseCardAction(rearguard);
		assertTrue(scn.IsType(rearguard, CardType.MINION));
		assertFalse(scn.IsRace(rearguard, Race.MAN));

		scn.SkipToAssignments();
		// The Scout and the Rearguard are left unassigned.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, axeman}, new PhysicalCardImpl[]{gimli, footmen});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(6, scn.GetStrength(axeman));
		assertEquals(3, scn.GetStrength(scout));
		assertTrue(scn.ShadowPlayAvailable(march));
		scn.ShadowPlayCard(march);
		// A [raider] Man in the skirmish and one assigned to none are offered; a [dunland] Man and a [raider]
		// minion who is no Man are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(axeman, scout));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(footmen, rearguard));
		scn.ShadowChooseCard(axeman);

		assertEquals(9, scn.GetStrength(axeman));
		assertEquals(3, scn.GetStrength(scout));
		assertInZone(Zone.DISCARD, march);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// 9 against Aragorn's 8: the Axeman wins and lives on into the next skirmish, which is a phase of its own.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertInZone(Zone.SHADOW_CHARACTERS, axeman);
		assertEquals(6, scn.GetStrength(axeman));
	}

	@Test
	public void WithSixCompanionsTheRaiderManIsStrengthPlusFive() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var march = scn.GetShadowCard("march");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(march);

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		// Six companions, Frodo among them.
		scn.MoveCompanionsToTable(aragorn, gimli, sam, merry, pippin);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, axeman);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(6, scn.GetStrength(axeman));
		scn.ShadowPlayCard(march);
		// The one [raider] Man is chosen unasked.
		assertEquals(11, scn.GetStrength(axeman));
		assertInZone(Zone.DISCARD, march);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoRaiderManTheEventIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var march = scn.GetShadowCard("march");
		var footmen = scn.GetShadowCard("footmen");
		scn.MoveCardsToHand(march);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(footmen);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, footmen);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(11, scn.GetStrength(footmen));
		assertTrue(scn.ShadowPlayAvailable(march));
		scn.ShadowPlayCard(march);

		assertEquals(11, scn.GetStrength(footmen));
		assertInZone(Zone.DISCARD, march);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotPlayableInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var march = scn.GetShadowCard("march");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(march);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(march));
	}
}
