package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_239_Tests
{

	// The Ring-bearer is Frodo, Son of Drogo, who is kept out of the skirmishes.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("menofrhun", "4_239");

					// Easterling Infantry: [raider] Easterling Man, strength 8; fierce and damage +1 only at 3 burdens
					put("infantry", "4_227");
					// Southron Explorer: [raider] Southron Man, strength 5, vitality 3; a regroup ability only, nothing about burdens
					put("explorer", "4_250");
					put("explorer2", "4_250");
					// Ulaire Attea, Khamul of the East: a [raider] minion who is no Man (a Nazgul), strength 12, fierce;
					// his strength bonus for Easterlings needs 3 burdens
					put("khamul", "103_61");
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man, strength 8, vitality 1; no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, strength 8; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6; a skirmish ability that is never used here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MenofRhunStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Men of Rhûn
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make a [raider] Man strength +3 (or +4 if you spot 4 burdens).
		*/

		var scn = GetScenario();

		var menofrhun = scn.GetFreepsCard("menofrhun");

		assertEquals("Men of Rhûn", menofrhun.getBlueprint().getTitle());
		assertNull(menofrhun.getBlueprint().getSubtitle());
		assertFalse(menofrhun.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, menofrhun.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, menofrhun.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, menofrhun.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(menofrhun, Timeword.SKIRMISH));
		assertEquals(2, menofrhun.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithNoBurdensARaiderManOfALaterSkirmishIsStrengthPlusThreeUntilThisSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var menofrhun = scn.GetShadowCard("menofrhun");
		var infantry = scn.GetShadowCard("infantry");
		var explorer = scn.GetShadowCard("explorer");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(menofrhun);
		assertInHand(menofrhun);

		scn.StartGame();
		scn.MoveMinionsToTable(infantry, explorer, khamul, brigand);
		assertEquals(0, scn.GetBurdens());
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// The phase before the skirmishes: not playable.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(menofrhun));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, infantry}, new PhysicalCardImpl[]{gimli, explorer});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(infantry));
		assertEquals(5, scn.GetStrength(explorer));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.ShadowPlayAvailable(menofrhun));

		scn.ShadowPlayCard(menofrhun);
		// A [raider] Man in this skirmish and one assigned to a later one, an Easterling and a Southron; not the
		// [raider] Nazgul, the [dunland] Man or the [gondor] Man companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(infantry, explorer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(khamul, brigand, aragorn, gimli, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(explorer);

		assertEquals(8, scn.GetStrength(explorer));
		assertEquals(8, scn.GetStrength(infantry));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(8, scn.GetStrength(brigand));
		assertInDiscard(menofrhun);
		// Still in force at the next decision of this skirmish.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(explorer));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);

		// The bonus was for that skirmish phase: in his own skirmish the Explorer is back to 5.
		assertEquals(5, scn.GetStrength(explorer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithExactlyFourBurdensARaiderManIsStrengthPlusFourUntilTheSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var menofrhun = scn.GetShadowCard("menofrhun");
		var explorer = scn.GetShadowCard("explorer");
		var explorer2 = scn.GetShadowCard("explorer2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(menofrhun);

		scn.StartGame();
		scn.AddBurdens(4);
		assertEquals(4, scn.GetBurdens());
		scn.MoveMinionsToTable(explorer, explorer2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, explorer}, new PhysicalCardImpl[]{gimli, explorer2});
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(explorer));
		assertEquals(5, scn.GetStrength(explorer2));
		scn.ShadowPlayCard(menofrhun);
		scn.ShadowChooseCard(explorer2);

		assertEquals(9, scn.GetStrength(explorer2));
		assertEquals(5, scn.GetStrength(explorer));
		assertInDiscard(menofrhun);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(explorer2));

		// 6 against 9: Gimli loses and takes a wound; in Aragorn's skirmish the bonus is gone.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(5, scn.GetStrength(explorer2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithThreeBurdensARaiderManIsStrengthPlusThreeOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var menofrhun = scn.GetShadowCard("menofrhun");
		var explorer = scn.GetShadowCard("explorer");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(menofrhun);

		scn.StartGame();
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());
		scn.MoveMinionsToTable(explorer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, explorer);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(explorer));
		scn.ShadowPlayCard(menofrhun);

		assertEquals(8, scn.GetStrength(explorer));
		assertInDiscard(menofrhun);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoRaiderManInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var menofrhun = scn.GetShadowCard("menofrhun");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(menofrhun);

		scn.StartGame();
		scn.MoveMinionsToTable(khamul, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, brigand);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(8, scn.GetStrength(aragorn));
		// Its target is no requirement of its play (CR 4.2 "effect").
		assertTrue(scn.ShadowPlayAvailable(menofrhun));

		scn.ShadowPlayCard(menofrhun);

		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(8, scn.GetStrength(aragorn));
		assertInDiscard(menofrhun);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
