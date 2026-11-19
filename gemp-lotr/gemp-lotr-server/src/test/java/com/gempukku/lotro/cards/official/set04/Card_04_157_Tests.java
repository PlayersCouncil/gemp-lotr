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

public class Card_04_157_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("leechcraft", "4_157");
					put("saruman", "3_68");     // Saruman, Keeper of Isengard: [isengard] Wizard, vitality 4; Uruk-hai are fierce; a wound response used only on purpose
					put("saruman2", "4_33");    // Saruman, Rabble-rouser: [dunland] Wizard, vitality 4; his move trigger counts [dunland] Men (none here)
					put("follower", "17_38");   // Saruman, Servant of Sauron: an [isengard] follower, so a Saruman who cannot exert
					put("grima", "4_154");      // Gríma, Wormtongue: [isengard] Man, vitality 3; a maneuver ability needing a companion bearing 3 cards
					put("traitor", "6_72");     // Rohirrim Traitor: [isengard] Man, vitality 2; strength +2 while a companion bears an [isengard] condition
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai who is no Man, vitality 3, Damage +1 and nothing else
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man with no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; Maneuver: exert Aragorn to make him defender +1
					put("albert", "1_69");      // Albert Dreary: [gandalf] Man ally; Maneuver: exert him to discard an [isengard] or [moria] condition
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an Elf companion, no Man
					put("blade", "15_62");      // Ithilien Blade: hand weapon on a [gondor] Man; Maneuver: exert bearer twice to discard a roaming minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LeechcraftStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Leechcraft
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: 
		 * Strength: -1
		 * Game Text: <b>Spell</b>. To play, exert Saruman or an [isengard] Man. Plays on a Free Peoples Man.<br>Special abilities in bearer's game text may not be used.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("leechcraft");

		assertEquals("Leechcraft", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(-1, card.getBlueprint().getStrength());
	}

	@Test
	public void ExertsSarumanOrAnIsengardManOfThePlayersChoiceToPlayOnAFreePeoplesMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leechcraft = scn.GetShadowCard("leechcraft");
		var saruman = scn.GetShadowCard("saruman");
		var grima = scn.GetShadowCard("grima");
		var traitor = scn.GetShadowCard("traitor");
		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");
		var aragorn = scn.GetFreepsCard("aragorn");
		var albert = scn.GetFreepsCard("albert");
		var arwen = scn.GetFreepsCard("arwen");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, arwen);
		scn.MoveAlliesToTable(albert);
		scn.MoveCardsToHand(leechcraft);
		assertInHand(leechcraft);

		scn.StartGame();
		// Laid before the Shadow phase begins, so that its first choice of actions takes them in.
		scn.MoveMinionsToTable(saruman, grima, traitor, savage, brigand);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(grima));
		assertEquals(0, scn.GetWoundsOn(traitor));
		assertTrue(scn.ShadowPlayAvailable(leechcraft));
		scn.ShadowPlayCard(leechcraft);

		// The bearer is asked first, then who exerts.
		// A Man companion and a Man ally; not an Elf companion, the Ring-bearer, or a Shadow Man.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, albert));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(arwen, frodo, grima));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(albert);

		// Saruman, and two [isengard] Men; not an [isengard] Uruk-hai or a [dunland] Man.
		assertTrue(scn.ShadowHasCardChoiceAvailable(saruman, grima, traitor));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, brigand));
		scn.ShadowChooseCard(saruman);
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(grima));
		assertEquals(0, scn.GetWoundsOn(traitor));
		assertEquals(0, scn.GetWoundsOn(savage));

		assertAttachedTo(leechcraft, albert);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTheIsengardManExhaustedTheDunlandSarumanExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leechcraft = scn.GetShadowCard("leechcraft");
		var saruman = scn.GetShadowCard("saruman2");
		var grima = scn.GetShadowCard("grima");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(leechcraft);
		assertInHand(leechcraft);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, grima);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(grima, 2);
		assertEquals(2, scn.GetWoundsOn(grima));
		assertEquals(0, scn.GetWoundsOn(saruman));
		scn.FreepsPass();

		scn.ShadowPlayCard(leechcraft);
		// Nothing asked: Saruman is the one who can pay, and Aragorn the one Free Peoples Man.
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(2, scn.GetWoundsOn(grima));
		assertAttachedTo(leechcraft, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithSarumanExhaustedAnIsengardManExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leechcraft = scn.GetShadowCard("leechcraft");
		var saruman = scn.GetShadowCard("saruman");
		var traitor = scn.GetShadowCard("traitor");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(leechcraft);
		assertInHand(leechcraft);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, traitor);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(saruman, 3);
		assertEquals(3, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(traitor));
		scn.FreepsPass();

		scn.ShadowPlayCard(leechcraft);
		assertEquals(1, scn.GetWoundsOn(traitor));
		assertEquals(3, scn.GetWoundsOn(saruman));
		assertAttachedTo(leechcraft, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotPlayableWithNoSarumanOrIsengardManAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leechcraft = scn.GetShadowCard("leechcraft");
		var follower = scn.GetShadowCard("follower");
		var grima = scn.GetShadowCard("grima");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		// A Saruman who is a follower, and so has no vitality to exert.
		scn.MoveCardsToSupportArea(follower);
		scn.MoveCardsToHand(leechcraft);
		assertInHand(leechcraft);
		assertInZone(Zone.SUPPORT, follower);

		scn.StartGame();
		scn.MoveMinionsToTable(grima, savage);
		scn.AddWoundsToChar(grima, 2);
		assertEquals(2, scn.GetWoundsOn(grima));
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.FreepsPass();

		// Gríma exhausted; the Uruk Savage, who could exert, is no Man.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(leechcraft));
		assertInHand(leechcraft);
	}

	@Test
	public void BearersOwnSpecialAbilityCannotBeUsedWhileTheAbilitiesOfHisPossessionAndOfAnotherManCanUntilItIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leechcraft = scn.GetShadowCard("leechcraft");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		var blade = scn.GetFreepsCard("blade");
		var albert = scn.GetFreepsCard("albert");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(albert);
		scn.AttachCardsTo(aragorn, blade, leechcraft);
		assertAttachedTo(blade, aragorn);
		assertAttachedTo(leechcraft, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(aragorn));
		// The Blade's ability is in the Blade's game text, though it exerts the bearer; Albert's is another Man's.
		assertTrue(scn.FreepsActionAvailable(blade));
		assertTrue(scn.FreepsActionAvailable(albert));

		// Albert discards Leechcraft, the one [isengard] condition.
		scn.FreepsUseCardAction(albert);
		assertEquals(1, scn.GetWoundsOn(albert));
		assertInDiscard(leechcraft);
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(aragorn));
	}

	@Test
	public void BorneByAnAllyHisSpecialAbilityCannotBeUsed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leechcraft = scn.GetShadowCard("leechcraft");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		var albert = scn.GetFreepsCard("albert");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(albert);
		scn.AttachCardsTo(albert, leechcraft);
		assertAttachedTo(leechcraft, albert);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(albert));
		assertTrue(scn.FreepsActionAvailable(aragorn));
	}
}
