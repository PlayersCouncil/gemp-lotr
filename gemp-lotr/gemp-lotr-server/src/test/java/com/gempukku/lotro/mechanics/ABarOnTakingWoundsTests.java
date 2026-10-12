package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A character who cannot take wounds takes none, whatever places them.
 *
 * Comprehensive Rules 4.2, "wound": "When you 'wound a character,' you place only one wound."  Wounds are placed by
 * a card's text and by four rules: the wounds of a lost skirmish, archery fire on each side, and the wounds a
 * companion's death sets off when there are threats ("threats").  The bar is the definition word `cantTakeWounds`,
 * which more printed cards lay than any other bar on an act (56 definitions, most of them for a phase or "except
 * during skirmishes"); every route that places a wound asks it, either as the choice of who is wounded
 * (`ChooseAndWoundCharactersEffect`, whose candidates are those who can take the wound) or as the wound is placed
 * (`WoundCharactersEffect`).  Each route is here once, with a printed card, and with a control: another character
 * the bar does not reach, wounded on the same board, or the same character wounded before the bar is in play.
 *
 * <ul>
 * <li>A card's `wound` with a choice: Hate 1_250 ("Maneuver: Exert a [sauron] Orc to wound a companion (except
 * the Ring-bearer)").</li>
 * <li>The wounds of a lost skirmish (`ResolveSkirmishDamageAction`, a `WoundCharactersEffect` with no choice).</li>
 * <li>The minion archery total, placed by the Free Peoples player on his companions.</li>
 * <li>The fellowship archery total, placed by the Shadow player on his minions.</li>
 * <li>Threat wounds (`ThreatRule`), placed by the Free Peoples player on his companions.</li>
 * </ul>
 *
 * The bars: Númenor's Pride 7_45 ("Gandalf is defender +1 and cannot take wounds"), for the Free Peoples side, and
 * Saruman, Black Traitor 4_173 ("Saruman may not take wounds during the archery phase ..."), for the minions'
 * archery wounds.
 *
 * Ketura on Q-0264 (2026-10-12): mechanics tests "should exist that do such a thing", "which will then ensure that
 * future cards using the bar only need to act as A": a card that lays this bar is tested with one wound it stops
 * and its control.
 */
public class ABarOnTakingWoundsTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Númenor's Pride: a [gandalf] condition of the support area.  "To play, spot Gandalf and add 3
					// threats.  Gandalf is defender +1 and cannot take wounds.  Discard this condition at the start
					// of the regroup phase."  Cheated in, so no threat is added.
					put("pride", "7_45");
					// Gandalf, Friend of the Shirefolk: strength 7, +1 for each of Hobbit, Dwarf, Elf and Man
					// spotted in the fellowship; vitality 4.
					put("gandalf", "1_72");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man, strength 8, vitality 4; a maneuver ability never used
					// Legolas, Greenleaf: an archer, strength 6, vitality 3; his archery ability is never used, so
					// he adds 1 to the fellowship archery total.
					put("legolas", "1_50");

					// Hate: a [sauron] Maneuver event, cost 0.  "Exert a [sauron] Orc to wound a companion (except
					// the Ring-bearer)."
					put("hate1", "1_250");
					put("hate2", "1_250");
					// Orc Inquisitor: a [sauron] Orc, vitality 3; its text is about being played.
					put("inquisitor", "1_268");
					put("troop1", "1_177");     // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("troop2", "1_177");
					put("marksman", "1_176");   // Goblin Marksman: an archer and nothing else
					// Saruman, Black Traitor: a Wizard minion, vitality 4.  "Saruman may not take wounds during the
					// archery phase and may not be assigned to a skirmish."  His other texts are about being played
					// and a Shadow ability, never used.
					put("saruman", "4_173");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ACardsWoundDoesNotOfferACharacterWhoCannotTakeWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pride = scn.GetFreepsCard("pride");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(gandalf, aragorn, legolas);
		// The bar waits in the discard pile and is put into play from there below.
		scn.MoveCardsToDiscard(pride);

		var hate1 = scn.GetShadowCard("hate1");
		var hate2 = scn.GetShadowCard("hate2");
		var inquisitor = scn.GetShadowCard("inquisitor");
		scn.MoveCardsToHand(hate1, hate2);

		scn.StartGame();
		scn.MoveMinionsToTable(inquisitor);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Without the bar, Gandalf is one of the three companions Hate may wound.
		scn.ShadowPlayCard(hate1);
		assertEquals(1, scn.GetWoundsOn(inquisitor));
		assertTrue(scn.ShadowHasCardChoiceAvailable(gandalf, aragorn, legolas));
		scn.ShadowChooseCard(gandalf);
		assertEquals(1, scn.GetWoundsOn(gandalf));

		scn.MoveCardsToSupportArea(pride);
		assertInZone(Zone.SUPPORT, pride);
		scn.FreepsPass();

		// Under it, he is not offered, and the others are.
		scn.ShadowPlayCard(hate2);
		assertEquals(2, scn.GetWoundsOn(inquisitor));
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, legolas));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gandalf));
		scn.ShadowChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gandalf));
	}

	@Test
	public void TheWoundOfALostSkirmishIsNotTakenByACharacterWhoCannotTakeWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pride = scn.GetFreepsCard("pride");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(pride);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, troop1}, new PhysicalCardImpl[]{aragorn, troop2});

		// Each companion loses to a Troop of 13, short of double his strength.
		scn.FreepsResolveSkirmish(gandalf);
		assertTrue(scn.GetStrength(gandalf) < 13);
		assertTrue(scn.GetStrength(gandalf) * 2 > 13);
		scn.PassSkirmishActions();
		// Gandalf lost and takes no wound.
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.FREE_CHARACTERS, gandalf);

		// The control: Aragorn loses the other and takes his.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.GetStrength(aragorn) < 13);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gandalf));
	}

	@Test
	public void TheMinionArcheryTotalIsNotPlacedOnACompanionWhoCannotTakeWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pride = scn.GetFreepsCard("pride");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(pride);

		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassArcheryActions();

		// The one wound of the minion archery total is the Free Peoples player's to place: Frodo and Aragorn are
		// offered, and Gandalf is not.
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gandalf));
	}

	@Test
	public void TheFellowshipArcheryTotalIsNotPlacedOnAMinionWhoCannotTakeWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		var saruman = scn.GetShadowCard("saruman");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, troop1, troop2);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassArcheryActions();

		// The one wound of the fellowship archery total is the Shadow player's to place: the two Troops are
		// offered, and Saruman is not.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop1, troop2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saruman));
		scn.ShadowChooseCard(troop1);
		assertEquals(1, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(saruman));
	}

	@Test
	public void AThreatWoundIsNotPlacedOnACompanionWhoCannotTakeWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pride = scn.GetFreepsCard("pride");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(gandalf, aragorn, legolas);
		scn.MoveCardsToSupportArea(pride);

		var hate1 = scn.GetShadowCard("hate1");
		var inquisitor = scn.GetShadowCard("inquisitor");
		scn.MoveCardsToHand(hate1);

		scn.StartGame();
		// Aragorn has one vitality left, so that the wound of Hate kills him.
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(inquisitor);
		scn.AddThreats(1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowPlayCard(hate1);
		scn.ShadowChooseCard(aragorn);
		assertInZone(Zone.DEAD, aragorn);

		// The one threat becomes one wound, the Free Peoples player's to place: Frodo and Legolas are offered,
		// and Gandalf is not.
		assertTrue(scn.FreepsDecisionAvailable("Choose characters to wound"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, legolas));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf));
		scn.FreepsChooseCard(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetThreats());
	}
}
