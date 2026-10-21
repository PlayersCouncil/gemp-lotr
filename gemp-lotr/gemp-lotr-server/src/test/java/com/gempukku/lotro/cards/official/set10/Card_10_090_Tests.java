package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_090_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("brute", "10_90");

					put("scimitar", "1_269");      // Orc Scimitar: [sauron] hand weapon on a [sauron] Orc; a Skirmish ability only
					put("chieftain", "1_266");     // Orc Chieftain: [sauron] Orc; answers only a weapon played on him
					put("warg", "10_89");          // Gothmog's Warg: [sauron] mount on a [sauron] Orc; strength for sites controlled, and none are
					put("pillager", "7_275");      // Gorgoroth Pillager: [sauron] Orc; its text works only while stacked on a site
					put("garrison", "7_273");      // Gorgoroth Garrison: [sauron] Orc; two Regroup abilities only
					put("enraged", "1_254");       // Mordor Enraged: [sauron] condition on a [sauron] minion, borne and no possession; a Shadow ability
					put("hound", "7_192");         // Morgul Hound: [wraith] Orc, an Orc who is not [sauron]
					put("nazgulscimitar", "7_203"); // Nazgul Scimitar: [wraith] hand weapon on a [wraith] minion; damage +1 only at 5 threats
					put("guard", "10_81");         // Cirith Ungol Guard: [sauron] Uruk-hai, a [sauron] minion who is no Orc; Damage +1 only
					put("axe", "10_102");          // Uruk Axe: [sauron] hand weapon on a [sauron] minion; +2 on an Uruk-hai

					put("gimli", "1_13");          // Gimli, Son of Gloin: the Dwarf who exerts for Baruk Khazad
					put("baruk", "5_5");           // Baruk Khazad: Maneuver event, "Exert a Dwarf to wound a minion"
					put("gandalf", "1_72");        // Gandalf, Friend of the Shirefolk: for Long I Fell
					put("fell", "4_97");           // Long I Fell: Skirmish event; the Shadow player may make the Free Peoples player wound a minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MordorBruteStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Mordor Brute
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: Each [sauron] Orc bearing a possession cannot take wounds (except during skirmish phases).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("brute");

		assertEquals("Mordor Brute", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachSauronOrcBearingAPossessionCannotTakeWoundsOutsideSkirmishes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var baruk = scn.GetFreepsCard("baruk");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(baruk);

		var brute = scn.GetShadowCard("brute");
		var scimitar = scn.GetShadowCard("scimitar");
		var chieftain = scn.GetShadowCard("chieftain");
		var warg = scn.GetShadowCard("warg");
		var pillager = scn.GetShadowCard("pillager");
		var garrison = scn.GetShadowCard("garrison");
		var enraged = scn.GetShadowCard("enraged");
		var hound = scn.GetShadowCard("hound");
		var nazgulscimitar = scn.GetShadowCard("nazgulscimitar");
		var guard = scn.GetShadowCard("guard");
		var axe = scn.GetShadowCard("axe");

		scn.StartGame();
		scn.MoveMinionsToTable(brute, chieftain, pillager, garrison, hound, guard);
		scn.AttachCardsTo(brute, scimitar);
		scn.AttachCardsTo(chieftain, warg);
		scn.AttachCardsTo(garrison, enraged);
		scn.AttachCardsTo(hound, nazgulscimitar);
		scn.AttachCardsTo(guard, axe);
		assertSame(brute, scimitar.getAttachedTo());
		assertSame(chieftain, warg.getAttachedTo());
		assertSame(garrison, enraged.getAttachedTo());
		assertSame(hound, nazgulscimitar.getAttachedTo());
		assertSame(guard, axe.getAttachedTo());
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(pillager));
		scn.FreepsPlayCard(baruk);
		assertEquals(1, scn.GetWoundsOn(gimli));

		// Barred: the Brute himself with a hand weapon, and an Orc on a mount.  Offered: a [sauron] Orc bearing nothing,
		// one bearing a condition, a [wraith] Orc with a possession, and a [sauron] Uruk-hai with a possession.
		assertTrue(scn.FreepsHasCardChoiceAvailable(pillager, garrison, hound, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(brute, chieftain));
		scn.FreepsChooseCard(pillager);

		assertEquals(1, scn.GetWoundsOn(pillager));
		assertEquals(0, scn.GetWoundsOn(brute));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithoutMordorBruteASauronOrcBearingAPossessionTakesTheWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var baruk = scn.GetFreepsCard("baruk");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(baruk);

		var chieftain = scn.GetShadowCard("chieftain");
		var scimitar = scn.GetShadowCard("scimitar");
		var pillager = scn.GetShadowCard("pillager");

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, pillager);
		scn.AttachCardsTo(chieftain, scimitar);
		assertSame(chieftain, scimitar.getAttachedTo());
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(chieftain));
		scn.FreepsPlayCard(baruk);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsHasCardChoiceAvailable(chieftain, pillager));
		scn.FreepsChooseCard(chieftain);

		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void DuringASkirmishASauronOrcBearingAPossessionInNoSkirmishTakesAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var fell = scn.GetFreepsCard("fell");
		scn.MoveCompanionsToTable(gandalf, gimli);
		scn.MoveCardsToHand(fell);

		var brute = scn.GetShadowCard("brute");
		var scimitar = scn.GetShadowCard("scimitar");
		var chieftain = scn.GetShadowCard("chieftain");
		var warg = scn.GetShadowCard("warg");

		scn.StartGame();
		scn.MoveMinionsToTable(brute, chieftain);
		scn.AttachCardsTo(brute, scimitar);
		scn.AttachCardsTo(chieftain, warg);
		assertSame(brute, scimitar.getAttachedTo());
		assertSame(chieftain, warg.getAttachedTo());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, chieftain}, new PhysicalCardImpl[]{gimli, brute});

		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(0, scn.GetWoundsOn(brute));
		scn.FreepsPlayCard(fell);
		scn.ShadowChooseYes();

		// The Brute waits for Gimli's skirmish and is in none now; during a skirmish phase he may be wounded.
		assertTrue(scn.FreepsHasCardChoiceAvailable(brute, chieftain));
		scn.FreepsChooseCard(brute);

		assertEquals(1, scn.GetWoundsOn(brute));
		assertSame(brute, scimitar.getAttachedTo());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
